package com.raju.domain.models

import android.os.Parcel
import android.os.Parcelable

data class DeveloperModel(
    val remote_dev_developer_name: String,
    val remote_dev_email_address: String,
    val remote_dev_profile_link: String,
    val remote_dev_mobile_number: String,
    val remote_dev_organisation_name:String,
    val remote_dev_publisher_link: String
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
        parcel.writeString(remote_dev_organisation_name)
        parcel.writeString(remote_dev_developer_name)
        parcel.writeString(remote_dev_profile_link)
        parcel.writeString(remote_dev_mobile_number)
        parcel.writeString(remote_dev_email_address)
        parcel.writeString(remote_dev_publisher_link)
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
