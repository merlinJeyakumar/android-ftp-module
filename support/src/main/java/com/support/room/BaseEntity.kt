package com.support.room

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.support.inline.orElse
import com.support.utills.DateConverter
import org.joda.time.DateTime


open class BaseEntity() : Parcelable {

    object Fields {
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

    constructor(parcel: Parcel) : this() {
        isActive = parcel.readByte() != 0.toByte()
        isDeleted = parcel.readByte() != 0.toByte()
        createdAt = DateTime(parcel.readLong())
        updatedAt = DateTime(parcel.readLong())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isActive) 1 else 0)
        parcel.writeByte(if (isDeleted) 1 else 0)
        parcel.writeLong(createdAt?.millis.orElse { 0L })
        parcel.writeLong(updatedAt?.millis.orElse { 0L })
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BaseEntity> {
        override fun createFromParcel(parcel: Parcel): BaseEntity {
            return BaseEntity(parcel)
        }

        override fun newArray(size: Int): Array<BaseEntity?> {
            return arrayOfNulls(size)
        }
    }

}