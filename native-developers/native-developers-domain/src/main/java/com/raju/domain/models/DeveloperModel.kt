package com.raju.domain.models

import android.os.Parcel
import android.os.Parcelable

data class DeveloperModel(
    val developerName: String,
    val developerEmail: String,
    val developerLink: String,
    val developerPhone: String,
    val organisationName:String,
    val publisherLink: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(organisationName)
        parcel.writeString(developerName)
        parcel.writeString(developerLink)
        parcel.writeString(developerPhone)
        parcel.writeString(developerEmail)
        parcel.writeString(publisherLink)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeveloperModel> {
        override fun createFromParcel(parcel: Parcel): DeveloperModel {
            return DeveloperModel(parcel)
        }

        override fun newArray(size: Int): Array<DeveloperModel?> {
            return arrayOfNulls(size)
        }
    }

}
