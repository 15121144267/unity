package com.tg.unitylibrary

import android.os.Parcel
import android.os.Parcelable
import com.alibaba.fastjson.JSONObject


/**
 * @author Created by helei
 * @data 27.10.21
 * Email:helei19910210@163.com
 * Description:
 */
data class UnityEvent(
    var action: String?,
    var gameEvent: String?,
    var gameEventId: String?,
    var gameData: JSONObject?
):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readSerializable() as JSONObject?
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(action)
        parcel.writeString(gameEvent)
        parcel.writeString(gameEventId)
        parcel.writeSerializable(gameData)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UnityEvent> {
        override fun createFromParcel(parcel: Parcel): UnityEvent {
            return UnityEvent(parcel)
        }

        override fun newArray(size: Int): Array<UnityEvent?> {
            return arrayOfNulls(size)
        }
    }

}