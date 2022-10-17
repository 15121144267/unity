package com.tg.unitylibrary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MainViewSetVisibilityEvent(
    var visiblility: Boolean
) : Parcelable
