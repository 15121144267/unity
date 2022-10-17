package com.tg.unitylibrary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MainTopViewSetVisibilityEvent(
    var visiblility: Boolean
) : Parcelable
