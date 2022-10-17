package com.tg.unitylibrary

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * @author Created by helei
 * @data 26.10.21
 * Email:helei19910210@163.com
 * Description:
 */
class UnityPluginService : Service() {
    private val binder: Binder = UnityPluginContext()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

}