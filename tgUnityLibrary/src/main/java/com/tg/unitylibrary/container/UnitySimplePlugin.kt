package com.tg.unitylibrary.container

import android.content.Intent
import androidx.annotation.CallSuper

abstract class UnitySimplePlugin : UnityPlugin {
    @CallSuper
    override fun onInitialize() {
    }

    override fun onResult(requestCode: Int, resultCode: Int, data: Intent) {}
}