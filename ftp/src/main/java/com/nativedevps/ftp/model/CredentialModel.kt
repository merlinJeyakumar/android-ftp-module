package com.nativedevps.ftp.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson

class CredentialModel(
    var address: String? = null,
    var userName: String? = null,
    var password: String? = null,
    var isAuthenticated: Boolean = false,
    var port: String? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(address)
        parcel.writeString(userName)
        parcel.writeString(password)
        parcel.writeByte(if (isAuthenticated) 1 else 0)
        parcel.writeString(port)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CredentialModel> {
        override fun createFromParcel(parcel: Parcel): CredentialModel {
            return CredentialModel(parcel)
        }

        override fun newArray(size: Int): Array<CredentialModel?> {
            return arrayOfNulls(size)
        }
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }
}