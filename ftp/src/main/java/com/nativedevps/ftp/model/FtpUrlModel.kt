package com.nativedevps.ftp.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson

class FtpUrlModel : Parcelable {
    var address: String? = null
    var userName: String? = null
    var password: String? = null
    var isAuthenticated: Boolean = false
    var port: String? = null
    var initialPath: String? = null
    var ftpAddress: String

    constructor() {
        ftpAddress = if (isAuthenticated) {
            "ftp://${this.userName}:${this.password}@${this.address}:${this.port}"
        } else {
            "ftp://${this.address}:${this.port}"
        }
    }

    constructor(
        address: String? = null,
        userName: String? = null,
        password: String? = null,
        isAuthenticated: Boolean = false,
        port: String? = null,
        initialPath: String? = null,
    ) {
        this.address = address
        this.userName = userName
        this.password = password
        this.isAuthenticated = isAuthenticated
        this.port = port
        this.initialPath = initialPath


        ftpAddress = if (isAuthenticated) {
            "ftp://${this.userName}:${this.password}@${this.address}:${this.port}"
        } else {
            "ftp://${this.address}:${this.port}"
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readString() ?: "") {
    }

    fun toJson(): String {
        return Gson().toJson(this)
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

    companion object CREATOR : Parcelable.Creator<FtpUrlModel> {
        override fun createFromParcel(parcel: Parcel): FtpUrlModel {
            return FtpUrlModel(parcel)
        }

        override fun newArray(size: Int): Array<FtpUrlModel?> {
            return arrayOfNulls(size)
        }

        fun fromJson(string: String): FtpUrlModel {
            return Gson().fromJson(string, FtpUrlModel::class.java).apply {
                this.ftpAddress = if (isAuthenticated) {
                    "ftp://${this.userName}:${this.password}@${this.address}:${this.port}"
                } else {
                    "ftp://${this.address}:${this.port}"
                } + initialPath
            }
        }

        fun fromUrl(url: String): FtpUrlModel {
            return FtpUrlModel().apply {
                ftpAddress = url

                Uri.parse(url).let {
                    val userInfo = it.userInfo?.split(":")?.toList().let {
                        Pair(it?.get(0) ?: "", it?.get(1) ?: "")
                    }
                    this.address = it.host
                    this.userName = userInfo.first
                    this.password = userInfo.second
                    this.isAuthenticated = !userInfo.second.isNullOrEmpty()
                    this.port = it.port.toString()
                }
            }
        }
    }

    fun getDirectory(prev: Boolean): String {
        var directory = ""
        Uri.parse(ftpAddress)
            .pathSegments?.toMutableList()?.let {
                if (prev) it.dropLast(2) else it
            }?.map { directory = "$directory/$it" }

        return ftpBaseAddress + directory.ifEmpty { "/" }
    }

    val ftpBaseAddress: String
        get() {
            return Uri.parse(ftpAddress).let {
                return@let "${it.scheme}://${it.authority}"
            }
        }

    val previousPathAddress:String get() = getDirectory(true)

    fun getRawPreviousPath(): String {
        return getDirectory(true).replace(ftpBaseAddress, "")
    }

    fun getRawPath(): String {
        return getDirectory(false).replace(ftpBaseAddress, "")
    }
}