package com.support.room

import androidx.room.*
import com.support.utills.DateConverter
import org.joda.time.DateTime


open class BaseEntity {

    object Fields {
        /*const val ID = "id"*/
        const val IS_DELETED = "is_deleted"
        const val IS_ACTIVE = "is_active"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }

    @ColumnInfo(name = Fields.IS_ACTIVE)
    var isActive: Boolean = false

    @ColumnInfo(name = Fields.IS_DELETED)
    var isDeleted: Boolean = false

    @TypeConverters(DateConverter::class)
    @ColumnInfo(name = Fields.CREATED_AT)
    var createdAt: DateTime? = null

    @TypeConverters(DateConverter::class)
    @ColumnInfo(name = Fields.UPDATED_AT)
    var updatedAt: DateTime? = null

}