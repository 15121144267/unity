package com.tg.unitylibrary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UnitySwitchSceneEvent(
    var viewId: Int,
    var gameEventId: String
) : Parcelable
