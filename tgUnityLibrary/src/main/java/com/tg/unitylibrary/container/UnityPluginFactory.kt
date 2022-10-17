package com.tg.unitylibrary.container

import com.tg.unitylibrary.plugin.UnityGameBasePlugin
import com.tg.unitylibrary.plugin.UnityGamePlugin

class UnityPluginFactory: UnityPluginManager.PluginFactory {
    override fun onPluginRegister(unityPluginManager: UnityPluginManager?) {
        unityPluginManager?.addPlugin(UnityGameBasePlugin::class.java, null)
        unityPluginManager?.addPlugin(UnityGamePlugin::class.java, null)
    }

    override val tag: String
        get() = "Unity"
}