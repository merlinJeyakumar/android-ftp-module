package com.raju.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.raju.data.database.dao.*
import com.raju.domain.entity.*

@Database(
    entities = arrayOf(
        QuickTextEntity::class
    ),
    version = 1,
    exportSchema = false
)
abstract class RoomDataSource : RoomDatabase() {

    abstract val quickTextItemDao: QuickTextItemDao


}
