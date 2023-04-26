package database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseManager private constructor() {

    private var openCounter = 0
    private lateinit var databaseHelper: SQLiteOpenHelper
    private lateinit var database: SQLiteDatabase

    companion object {
        @Volatile
        private var instance: DatabaseManager? = null

        @Synchronized
        fun initializeInstance(helper: SQLiteOpenHelper) {
            if (instance == null) {
                instance = DatabaseManager()
                instance!!.databaseHelper = helper
            }
        }

        @Synchronized
        fun getInstance(): DatabaseManager {
            if (instance == null) {
                throw IllegalStateException("${DatabaseManager::class.simpleName} is not initialized, call initializeInstance(..) method first.")
            }
            return instance!!
        }
    }

    @Synchronized
    fun openDatabase(): SQLiteDatabase {
        openCounter++
        if (openCounter == 1) {
            database = databaseHelper.writableDatabase
        }
        return database
    }
}