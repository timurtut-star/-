package ru.wellservice.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

data class WellAsset(
    val number: String,
    val motor: String,
    val pump: String,
    val level: String,
    val converter: String
)

class MainActivity : Activity() {
    private lateinit var db: WellDb
    private lateinit var list: LinearLayout
    private lateinit var empty: TextView
    private lateinit var detail: LinearLayout
    private lateinit var wellPicker: Spinner
    private var selectedWellName: String? = null
    private lateinit var rootLayout: LinearLayout

    private val initialAssets = listOf(
        WellAsset("1Б", "ДАП 8-15x; 15 кВт; 33 А", "CRS 8-40/6; Q=40; H=90 м", "E+H Waterpilot FMX21; 0–40 м вод. ст.; 4–20 мА", "FR 740-00 380 EC"),
        WellAsset("2А", "MS6000T40; 9,2 кВт; 21,8 А", "SP30-10", "SG-25; 0–40 м вод. ст.; 4–20 мА", "FR 740-00 250 EC"),
        WellAsset("3А", "MS6000T40; 9,2 кВт; 21,8 А", "SP30-10", "SG-25; 0–40 м вод. ст.; 4–20 мА", "FR 740-00 250 EC"),
        WellAsset("3Б", "MS6000T40; 15 кВт; 34,5 А", "SP30-12", "E+H Waterpilot FMX21; 0–40 м вод. ст.; 4–20 мА", "FR 740-00 310 EC"),
        WellAsset("4А", "JETEX MC6; 15 кВт; 32,2 А", "JETEX C6 SS3 45-9; Q=46; H=79 м", "E+H Waterpilot FMX21; 0–40 м вод. ст.; 4–20 мА", "FR 740-00 380 EC"),
        WellAsset("5А", "ДАП 6-7,5x; 7,5 кВт; 18 А", "CRS 6-16/9; Q=16; H=80 м", "0–16 м вод. ст.; 4–20 мА", "FR 740-00 310 EC"),
        WellAsset("6Б", "MS6000T40; 11 кВт; 25 А", "SP30-10", "SG-25; 0–40 м вод. ст.; 4–20 мА", "FR 840-00380 E2-60; F800-E"),
        WellAsset("7", "JETEX MC6; 15 кВт; 33,1 А", "JETEX C6 SS3 45-10", "SG-25; 0–25 м вод. ст.; 4–20 мА", "FR 840-00380 E2-60; F800-E"),
        WellAsset("8Б", "ДАП 6-11x; 11 кВт; 25 А", "CRS 8-25/5; Q=25; H=90 м", "SG-25; 0–15 м вод. ст.; 4–20 мА", "FR 840-00380 EC"),
        WellAsset("9Б", "ДАП 6-7,5x; 7,5 кВт; 18 А", "CRS 6-16/9; Q=16; H=80 м", "0–16 м вод. ст.; 4–20 мА", "FR 840-00380-2-60; F800-E"),
        WellAsset("11Б", "ДАП 8-15x; 15 кВт; 33 А", "CRS 8-40/6; Q=40; H=90 м", "0–15 м вод. ст.; 4–20 мА", "FR 840-00380 E2-60; F800-E"),
        WellAsset("12", "ДАП 8-15x; 15 кВт; 33 А", "CRS 8-40/6; Q=40; H=90 м", "0–16 м вод. ст.; 4–20 мА", "FR 840-00380 E2-60; F800-E"),
        WellAsset("13", "MS6000T40; 9,2 кВт; 21,8 А", "SP30-9", "0–40 м вод. ст.; 4–20 мА", "FR 740-00 250 EC"),
        WellAsset("14", "MS6000T40; 9,2 кВт; 21,8 А", "SP30-9", "SG-25 Smart; 0–25 м; 4–20 мА", "FR 740-00 250 EC"),
        WellAsset("15", "MS6000T40; 5,5 кВт; 14,4 А", "SP17-8", "E+H Waterpilot FMX21; 0–40 м вод. ст.; 4–20 мА", "FR 740-00 250 EC"),
        WellAsset("16", "JETEX MC6; 15 кВт; 32,2 А", "SP46-10", "SG-25 Smart; 0–16 м; 4–20 мА", "FR 740-00 380 EC"),
        WellAsset("17", "MS6000T40; 9,2 кВт; 21,8 А", "SP30-10", "E+H Waterpilot FMX21; 0–40 м вод. ст.; 4–20 мА", "FR 740-00 250 EC"),
        WellAsset("18", "MS6000T40; 9,2 кВт; 21,8 А", "SP30-10", "SG-25 Smart; 0–16 м; 4–20 мА", "FR 740-00 250 EC"),
        WellAsset("19", "MS6000PQXT60; 15 кВт; 33,4 А", "SPE46-9; Q=46; H=87 м", "SG25/4-20/0–25 м; L=30 м", "C440-018x-1-ППК0-0.03КУ"),
        WellAsset("20", "MS6000PQXT60; 15 кВт; 33,4 А", "SPE46-9; Q=46; H=87 м", "SG25/4-20/0–25 м; L=30 м", "C440-018x-1-ППК0-0.03КУ"),
        WellAsset("21", "ДАП 6-7,5x; 7,5 кВт; 18 А", "CRS 6-25/10; Q=25; H=80 м", "E+H Waterpilot FMX21; 0–40 м вод. ст.; 4–20 мА", "FR 840-00380 E2-60; F800-E"),
        WellAsset("ДОП", "ДАП 6-11x; 11 кВт; 25 А", "CRS 6-16/16; Q=16; H=140 м", "E+H Waterpilot FMX21; 0–40 м вод. ст.; 4–20 мА; кабель 50 м", "MP-204")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = WellDb(this)
        db.seedIfEmpty(initialAssets.map { InstalledAsset(it.number, it.pump, it.motor, it.level, it.converter) })
        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 12)
            setBackgroundColor(0xFFF3F7F6.toInt())
        }
        setContentView(rootLayout)
        showMainMenu()
    }

    private fun prepareScreen(title: String, subtitle: String? = null) {
        rootLayout.removeAllViews()
        rootLayout.addView(TextView(this).apply {
            text = title
            textSize = 22f
            setTextColor(0xFF163D36.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        if (subtitle != null) rootLayout.addView(TextView(this).apply {
            text = subtitle
            textSize = 13f
            setTextColor(0xFF60736E.toInt())
            setPadding(0, 5, 0, 10)
        })
    }

    private fun showMainMenu() {
        selectedWellName = null
        prepareScreen("Обслуживание водяных скважин", "Главное меню")
        rootLayout.addView(Button(this).apply {
            text = "Скважины"
            textSize = 18f
            setOnClickListener { showWellList() }
        })
        rootLayout.addView(Button(this).apply {
            text = "Журнал обслуживания"
            textSize = 18f
            setOnClickListener { showJournalScreen() }
        })
    }

    private fun showWellList() {
        prepareScreen("Скважины", "Выберите номер, чтобы открыть карточку скважины")
        rootLayout.addView(Button(this).apply {
            text = "← Главное меню"
            setOnClickListener { showMainMenu() }
        })
        val scroll = ScrollView(this)
        val items = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        db.allAssets().forEach { asset ->
            items.addView(Button(this).apply {
                text = "Скважина № ${asset.wellName}"
                textSize = 17f
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                setPadding(18, 12, 18, 12)
                setOnClickListener { openWell(asset.wellName) }
            })
        }
        scroll.addView(items)
        rootLayout.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
    }

    private fun openWell(wellName: String) {
        selectedWellName = wellName
        val asset = db.asset(wellName) ?: return
        prepareScreen("Скважина № $wellName")
        rootLayout.addView(Button(this).apply {
            text = "← К списку скважин"
            setOnClickListener { showWellList() }
        })
        val detailScroll = ScrollView(this)
        detail = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 12, 14, 12)
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        detailScroll.addView(detail)
        rootLayout.addView(detailScroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        showAsset(WellAsset(asset.wellName, asset.motor, asset.pump, asset.level, asset.converter))
        rootLayout.addView(Button(this).apply {
            text = "Журнал обслуживания этой скважины"
            setOnClickListener { showJournalScreen(wellName) }
        })
    }

    private fun showJournalScreen(filterWell: String? = null) {
        selectedWellName = filterWell
        prepareScreen(
            if (filterWell == null) "Журнал обслуживания" else "Журнал скважины № $filterWell",
            if (filterWell == null) "Записи по всем скважинам" else "Записи только по выбранной скважине"
        )
        rootLayout.addView(Button(this).apply {
            text = if (filterWell == null) "← Главное меню" else "← К скважине № $filterWell"
            setOnClickListener {
                if (filterWell == null) showMainMenu() else openWell(filterWell)
            }
        })
        rootLayout.addView(Button(this).apply {
            text = "+ Добавить запись обслуживания"
            setOnClickListener { showAddDialog(filterWell) }
        })
        empty = TextView(this).apply {
            text = "Записей обслуживания пока нет"
            textSize = 14f
            setTextColor(0xFF60736E.toInt())
            setPadding(4, 8, 4, 8)
        }
        rootLayout.addView(empty)
        val journalScroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        journalScroll.addView(list)
        rootLayout.addView(journalScroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        refresh(filterWell)
    }

    private fun showAsset(a: WellAsset) {
        detail.removeAllViews()
        detail.addView(TextView(this).apply {
            text = "Скважина № ${a.number}"
            textSize = 20f
            setTextColor(0xFF176B5B.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 10)
        })
        assetLine("НАСОС", a.pump)
        assetLine("ЭЛЕКТРОДВИГАТЕЛЬ", a.motor)
        assetLine("ДАТЧИК УРОВНЯ", a.level)
        assetLine("ЧАСТОТНЫЙ ПРЕОБРАЗОВАТЕЛЬ / БЛОК", a.converter)
        detail.addView(TextView(this).apply {
            text = "Текущая комплектация. После замены оборудования здесь будут отображаться новые данные."
            textSize = 11f
            setTextColor(0xFF71817C.toInt())
            setPadding(0, 12, 0, 6)
        })
        detail.addView(Button(this).apply {
            text = "Изменить комплектацию"
            setOnClickListener { showEditAssetDialog(a) }
        })
        detail.addView(Button(this).apply {
            text = "История изменений оборудования"
            setOnClickListener { showAssetHistory(a.number) }
        })
    }


    private fun showEditAssetDialog(a: InstalledAsset) {
        val fields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 8, 22, 0)
        }
        fun field(label: String, value: String): EditText {
            val input = EditText(this).apply {
                hint = label
                setText(value)
                minLines = 1
            }
            fields.addView(input)
            return input
        }
        val pump = field("Установленный насос", a.pump)
        val motor = field("Электродвигатель", a.motor)
        val level = field("Уровнемер / датчик уровня", a.level)
        val converter = field("Частотный преобразователь / блок", a.converter)
        val note = field("Причина изменения (например, замена)", "")
        AlertDialog.Builder(this)
            .setTitle("Комплектация скважины № ${a.wellName}")
            .setView(ScrollView(this).apply { addView(fields) })
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить") { _, _ ->
                val updated = InstalledAsset(a.wellName,
                    pump.text.toString().trim(), motor.text.toString().trim(),
                    level.text.toString().trim(), converter.text.toString().trim())
                db.updateAsset(updated, note.text.toString().trim())
                openWell(updated.wellName)
                Toast.makeText(this, "Новая комплектация сохранена", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun showAssetHistory(wellName: String) {
        val entries = db.assetHistory(wellName)
        val message = if (entries.isEmpty()) {
            "История изменений пока пуста."
        } else entries.joinToString("\\n\\n") { h ->
            "${h.date}\\n${h.note}\\nНасос: ${h.pump}\\nДвигатель: ${h.motor}\\nУровнемер: ${h.level}\\nПреобразователь / блок: ${h.converter}"
        }
        AlertDialog.Builder(this)
            .setTitle("История комплектации № $wellName")
            .setMessage(message)
            .setPositiveButton("Закрыть", null)
            .show()
    }

    private fun assetLine(label: String, value: String) {
        detail.addView(TextView(this).apply {
            text = label
            textSize = 11f
            setTextColor(0xFF71817C.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 8, 0, 2)
        })
        detail.addView(TextView(this).apply {
            text = value
            textSize = 15f
            setTextColor(0xFF243C36.toInt())
        })
    }

    private fun showAddDialog(forWell: String? = null) {
        val fields = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 8, 22, 0) }
        fun field(hint: String, initial: String = ""): EditText = EditText(this).also {
            it.hint = hint; it.setText(initial); fields.addView(it)
        }
        val selected = forWell ?: selectedWellName ?: db.allAssets().first().wellName
        val well = field("Номер скважины", selected)
        val date = field("Дата обслуживания")
        val type = field("Вид работ")
        val desc = field("Что выполнено")
        val result = field("Результат / состояние")
        val next = field("Следующая дата обслуживания")
        AlertDialog.Builder(this).setTitle("Новая запись")
            .setView(ScrollView(this).apply { addView(fields) })
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = well.text.toString().trim()
                val d = date.text.toString().trim()
                val t = type.text.toString().trim()
                if (name.isEmpty() || d.isEmpty() || t.isEmpty()) {
                    Toast.makeText(this, "Заполните номер скважины, дату и вид работ", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                db.add(ServiceRecord(0, name, d, t, desc.text.toString().trim(),
                    result.text.toString().trim(), next.text.toString().trim()))
                refresh()
                Toast.makeText(this, "Запись сохранена", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun refresh(filterWell: String? = null) {
        list.removeAllViews()
        val records = db.all().filter { filterWell == null || it.wellName == filterWell }
        empty.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        records.forEach { r ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 10, 12, 8)
                setBackgroundColor(0xFFFFFFFF.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 5, 0, 5) }
            }
            card.addView(TextView(this).apply {
                text = "Скважина ${r.wellName} • ${r.date}"
                textSize = 16f
                setTextColor(0xFF163D36.toInt())
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            journalLine(card, "Работы: ${r.workType}")
            if (r.description.isNotBlank()) journalLine(card, "Описание: ${r.description}")
            if (r.result.isNotBlank()) journalLine(card, "Результат: ${r.result}")
            if (r.nextDate.isNotBlank()) journalLine(card, "Следующее обслуживание: ${r.nextDate}")
            card.addView(Button(this).apply {
                text = "Удалить запись"
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity).setMessage("Удалить эту запись?")
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Удалить") { _, _ -> db.delete(r.id); refresh() }.show()
                }
            })
            list.addView(card)
        }
    }

    private fun journalLine(parent: LinearLayout, value: String) {
        parent.addView(TextView(this).apply {
            text = value
            textSize = 13f
            setTextColor(0xFF435650.toInt())
            setPadding(0, 4, 0, 0)
        })
    }
}
