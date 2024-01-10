package com.aatif.happyplacesapp.models

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class HappyPlaceModel(
    val id: Int,
    val title: String,
    val description: String,
    val location: Location,
    val date: String,
    val imageUrl: String?=null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readParcelable<Location>(Location::class.java.classLoader) as Location,
        parcel.readString().orEmpty(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeParcelable(location, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        parcel.writeString(date)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HappyPlaceModel> {
        override fun createFromParcel(parcel: Parcel): HappyPlaceModel {
            return HappyPlaceModel(parcel)
        }

        override fun newArray(size: Int): Array<HappyPlaceModel?> {
            return arrayOfNulls(size)
        }
    }
}
