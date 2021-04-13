package com.support.room

import androidx.room.*


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

    @ColumnInfo(name = Fields.CREATED_AT)
    var createdAt: Long = 0L

    @ColumnInfo(name = Fields.UPDATED_AT)
    var updatedAt: Long = 0L

    /*@TypeConverters(DateConverter::class)
    @ColumnInfo(name = Fields.CREATE_AT)
    var createAt: Date? = null

    @TypeConverters(DateConverter::class)
    @ColumnInfo(name = Fields.UPDATE_AT)
    var updateAt: Date? = null*/


}