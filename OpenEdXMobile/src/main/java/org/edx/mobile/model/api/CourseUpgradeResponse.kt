package org.edx.mobile.model.api

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class CourseUpgradeResponse(source: Parcel) : Parcelable {
    @SerializedName("show_upsell")
    var showUpsell: Boolean = false

    @SerializedName("price")
    var price: String? = null

    @SerializedName("basket_url")
    var basketUrl: String?

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CourseUpgradeResponse> = object : Parcelable.Creator<CourseUpgradeResponse> {
            override fun createFromParcel(source: Parcel): CourseUpgradeResponse = CourseUpgradeResponse(source)
            override fun newArray(size: Int): Array<CourseUpgradeResponse?> = arrayOfNulls(size)
        }
    }

    init {
        this.showUpsell = 1 == source.readInt()
        this.price = source.readString()
        this.basketUrl = source.readString()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt((if (showUpsell) 1 else 0))
        writeString(price)
        writeString(basketUrl)
    }
}
