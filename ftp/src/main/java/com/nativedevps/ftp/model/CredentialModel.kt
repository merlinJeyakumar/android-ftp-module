package com.nativedevps.ftp.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson

class CredentialModel(
    var address: String? = null,
    var userName: String? = null,
    var password: String? = null,
    var isAuthenticated: Boolean = false,
    var port: String? = null,
    var initialPath: String? = null,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readByte() != 0.toByte(),
        parcel.readString()?:"",
        parcel.readString()?:"") {
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }

    fun fromUrl(url: String): CredentialModel {
        Uri.parse(url).let {
            val userInfo = it.userInfo?.split(":")?.toList().let {
                Pair(it?.get(0) ?: "", it?.get(1) ?: "")
            }
            this.address = it.host
            this.userName = userInfo.first
            this.password = userInfo.second
            this.isAuthenticated = !userInfo.second.isNullOrEmpty()
            this.port = it.port.toString()
            return this
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(address)
        parcel.writeString(userName)
        parcel.writeString(password)
        parcel.writeByte(if (isAuthenticated) 1 else 0)
        parcel.writeString(port)
        parcel.writeString(initialPath)
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

        fun fromJson(string: String): CredentialModel {
            return Gson().fromJson(string,CredentialModel::class.java)
        }
    }
}