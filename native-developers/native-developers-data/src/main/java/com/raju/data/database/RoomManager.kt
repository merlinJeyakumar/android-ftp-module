package com.raju.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class RoomManager {

    companion object {

        private var INSTANCE: RoomDataSource? = null

        private val DB_NAME = "MyApp-DB" //TODO: RENAME

        @JvmStatic
        fun getInstance(applicationContext: Context): RoomDataSource {
            if (INSTANCE == null) {
                synchronized(RoomManager::javaClass) {
                    INSTANCE = create(applicationContext)
                }
            }
            return INSTANCE!!
        }

        private fun create(context: Context): RoomDataSource {
            return Room.databaseBuilder(
                context,
                RoomDataSource::class.java,
                DB_NAME
            ).addMigrations(
                //MIGRATION_1_2
            ).allowMainThreadQueries().build()
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tbl_contact ADD COLUMN is_system_user INTEGER DEFAULT 0 NOT NULL")
            }
        }
    }
}