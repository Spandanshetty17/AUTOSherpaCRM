package database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap
import retrointerface.AudioDataResponse


class DBHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "WYZCRM.db"
        var DATABASE_VERSION = 2
        const val FILE_SYNC_TABLE = "filesync"
        const val FILE_ID = "FILE_ID"
        const val FILE_NAME = "FILE_NAME"
        const val SYNC_STATUS = "SYNC_STATUS"
        const val FILE_PATH = "FILE_PATH"
        const val DELETE_STATUS = "DELETE_STATUS"
        const val UNIQUE_ID = "UNIQUE_ID"
        private const val UPGRADE_FILESYNC_TABLE =
            "ALTER TABLE filesync ADD COLUMN AddDate DATETIME"
    }

    private val hp = HashMap<String, String>()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $FILE_SYNC_TABLE ($FILE_ID INTEGER PRIMARY KEY, $FILE_NAME TEXT, $SYNC_STATUS TEXT, $FILE_PATH TEXT, $DELETE_STATUS TEXT, $UNIQUE_ID TEXT, AddDate DATETIME DEFAULT CURRENT_TIMESTAMP)")
    }

    @SuppressLint("Range")
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        var upgradeTo = oldVersion
        try {
            Log.d("", "Old Version : $oldVersion and new Version :$newVersion")
            while (upgradeTo != newVersion) {
                Log.d(
                    "",
                    "Old Version : $oldVersion and new Version :$newVersion Upgrade to :$upgradeTo"
                )
                when (upgradeTo) {
                    1 -> {
                        db.execSQL(UPGRADE_FILESYNC_TABLE)
                        val cv = ContentValues()
                        cv.put("AddDate", getNowTimeString())
                        db.update("filesync", cv, null, null)
                    }
                    else -> {
                        db.execSQL("DROP TABLE IF EXISTS $FILE_SYNC_TABLE")
                        db.execSQL("DROP TABLE IF EXISTS recordsync")
                        onCreate(db)
                    }
                }
                upgradeTo++
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            throw e
        }
    }

    fun insertDetails(
        UNIQUE_ID: String,
        SYNC_STATUS: String,
        FILE_PATH: String,
        DELETE_STATUS: String
    ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("UNIQUE_ID", UNIQUE_ID)
        contentValues.put("SYNC_STATUS", SYNC_STATUS)
        contentValues.put("FILE_PATH", FILE_PATH)
        contentValues.put("DELETE_STATUS", DELETE_STATUS)
        db.insert(FILE_SYNC_TABLE, null, contentValues)
        return true
    }

    fun getData(id: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM filesync WHERE UNIQUE_ID=?", arrayOf(id.toString()))
    }

    fun numberOfRows(): Int {
        val db = this.readableDatabase
        return DatabaseUtils.queryNumEntries(db, FILE_SYNC_TABLE).toInt()
    }
    fun updateSyncStatus(uniqueId: String, syncStatus: String): Boolean {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            val contentValues = ContentValues()
            contentValues.put("SYNC_STATUS", "1")
            val result = db.update(FILE_SYNC_TABLE, contentValues, "UNIQUE_ID = ?", arrayOf(uniqueId))
            db.setTransactionSuccessful()
        } catch (ex: SQLiteDiskIOException) {
            ex.printStackTrace()
        } catch (sfe: SQLiteFullException) {
            sfe.printStackTrace()
        } finally {
            db.endTransaction()
        }
        return true
    }

    fun getNotSyncedFile(): ArrayList<String> {
        val file_list = ArrayList<String>()
        val cursor: Cursor = writableDatabase.rawQuery(
            "SELECT UNIQUE_ID FROM filesync where SYNC_STATUS='0'", null
        )
        if (cursor.moveToFirst()) {
            do {
                file_list.add(cursor.getString(cursor.getColumnIndex("UNIQUE_ID")))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return file_list
    }

    fun getNotSyncedFileAll(): ArrayList<String> {
        val file_list = ArrayList<String>()
        val db: SQLiteDatabase = DatabaseManager.getInstance().openDatabase()
        val selectQuery = "SELECT UNIQUE_ID FROM filesync where SYNC_STATUS='0'"
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                file_list.add(cursor.getString(cursor.getColumnIndex("UNIQUE_ID")))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return file_list
    }

    fun delete7DaysOldAudioRecords() {
        try {
            writableDatabase.execSQL(
                "DELETE FROM filesync WHERE AddDate <=  '" + getSevenDaysDateString() + "'  and (SYNC_STATUS = '1')"
            )
        } catch (se: SQLiteDiskIOException) {
            se.printStackTrace()
        }
    }

    private fun getSevenDaysDateString(): String {
        val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        val cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.DATE, -7)
        return dateFormat.format(cal.time)
    }

    private fun getThreeDaysDateString(): String {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.DATE, -3)
        return dateFormat.format(cal.time)
    }

    private fun getNowTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, 0)
        return dateFormat.format(cal.time)
    }
    fun deleteRecordingFiles() {
        val query1 = "SELECT FILE_PATH FROM filesync where AddDate <= '$getThreeDaysDateString()' and SYNC_STATUS = '1'"
        try {
            val cursor = writableDatabase.rawQuery(query1, null)
            if (cursor.count > 0 && cursor.moveToFirst()) {
                do {
                    //val dbfileOnSD = Environment.getExternalStorageDirectory().absolutePath + File.separator + "Ninja_Crm"
                    val file = File(cursor.getString(cursor.getColumnIndex("FILE_PATH")))
                    if (file.exists()) {
                        val isDeleted = file.delete()

                        if (isDeleted) {
                            Log.d("deleted file :", file.delete().toString())
                        }
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteDiskIOException) {
            e.printStackTrace()
        }
    }
}