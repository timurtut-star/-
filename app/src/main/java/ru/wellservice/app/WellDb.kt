package ru.wellservice.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ServiceRecord(
    val id: Long, val wellName: String, val date: String, val workType: String,
    val description: String, val result: String, val nextDate: String
)

data class InstalledAsset(
    val wellName: String, val pump: String, val motor: String,
    val level: String, val converter: String
)

data class AssetHistory(
    val date: String, val note: String, val pump: String,
    val motor: String, val level: String, val converter: String
)

class WellDb(context: Context) : SQLiteOpenHelper(context, "wells.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE service_records (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            well_name TEXT NOT NULL, date TEXT NOT NULL, work_type TEXT NOT NULL,
            description TEXT NOT NULL, result TEXT NOT NULL, next_date TEXT NOT NULL
        )""".trimIndent())
        createAssetTables(db)
    }

    private fun createAssetTables(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS well_assets (
            well_name TEXT PRIMARY KEY, pump TEXT NOT NULL, motor TEXT NOT NULL,
            level TEXT NOT NULL, converter TEXT NOT NULL
        )""".trimIndent())
        db.execSQL("""CREATE TABLE IF NOT EXISTS asset_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT, well_name TEXT NOT NULL,
            changed_at TEXT NOT NULL, note TEXT NOT NULL, pump TEXT NOT NULL,
            motor TEXT NOT NULL, level TEXT NOT NULL, converter TEXT NOT NULL
        )""".trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createAssetTables(db)
    }

    fun seedIfEmpty(items: List<InstalledAsset>) {
        if (readableDatabase.rawQuery("SELECT COUNT(*) FROM well_assets", null).use {
                it.moveToFirst(); it.getInt(0)
            } > 0) return
        writableDatabase.beginTransaction()
        try {
            items.forEach { item ->
                val v = ContentValues().apply {
                    put("well_name", item.wellName); put("pump", item.pump)
                    put("motor", item.motor); put("level", item.level); put("converter", item.converter)
                }
                writableDatabase.insertWithOnConflict("well_assets", null, v, SQLiteDatabase.CONFLICT_IGNORE)
            }
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }

    fun allAssets(): List<InstalledAsset> {
        val out = mutableListOf<InstalledAsset>()
        readableDatabase.rawQuery("SELECT well_name,pump,motor,level,converter FROM well_assets ORDER BY well_name", null).use { c ->
            while (c.moveToNext()) out += InstalledAsset(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4))
        }
        return out
    }

    fun asset(wellName: String): InstalledAsset? =
        allAssets().firstOrNull { it.wellName == wellName }

    fun updateAsset(a: InstalledAsset, note: String) {
        val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        writableDatabase.beginTransaction()
        try {
            val h = ContentValues().apply {
                put("well_name", a.wellName); put("changed_at", now)
                put("note", note.ifBlank { "Комплектация обновлена" })
                put("pump", a.pump); put("motor", a.motor); put("level", a.level); put("converter", a.converter)
            }
            writableDatabase.insert("asset_history", null, h)
            val v = ContentValues().apply {
                put("well_name", a.wellName); put("pump", a.pump)
                put("motor", a.motor); put("level", a.level); put("converter", a.converter)
            }
            writableDatabase.insertWithOnConflict("well_assets", null, v, SQLiteDatabase.CONFLICT_REPLACE)
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }

    fun assetHistory(wellName: String): List<AssetHistory> {
        val out = mutableListOf<AssetHistory>()
        readableDatabase.rawQuery(
            "SELECT changed_at,note,pump,motor,level,converter FROM asset_history WHERE well_name=? ORDER BY id DESC",
            arrayOf(wellName)
        ).use { c ->
            while (c.moveToNext()) out += AssetHistory(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5))
        }
        return out
    }

    fun add(r: ServiceRecord) {
        val v = ContentValues().apply {
            put("well_name", r.wellName); put("date", r.date); put("work_type", r.workType)
            put("description", r.description); put("result", r.result); put("next_date", r.nextDate)
        }
        writableDatabase.insert("service_records", null, v)
    }

    fun all(): List<ServiceRecord> {
        val out = mutableListOf<ServiceRecord>()
        readableDatabase.rawQuery(
            "SELECT id,well_name,date,work_type,description,result,next_date FROM service_records ORDER BY id DESC", null
        ).use { c ->
            while (c.moveToNext()) out += ServiceRecord(
                c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
                c.getString(4), c.getString(5), c.getString(6)
            )
        }
        return out
    }

    fun delete(id: Long) {
        writableDatabase.delete("service_records", "id=?", arrayOf(id.toString()))
    }
}
