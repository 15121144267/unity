package com.tg.unitylibrary.container

import android.text.TextUtils
import java.lang.reflect.InvocationTargetException
import java.util.*

class UnityPluginManager {

    private val pluginMap = HashMap<String, MutableList<UnityPlugin>?>()

    fun addPlugin(pluginClass: Class<out UnityPlugin>, argument: Any?) = try {
        val unityPlugin: UnityPlugin = if (argument == null) {
            pluginClass.newInstance()
        } else {
            pluginClass.getDeclaredConstructor(argument.javaClass).newInstance(argument)
        }
        val cocosEventFilter = UnityEventFilter()
        unityPlugin.onPrepare(cocosEventFilter)
        val iterator = cocosEventFilter.actionIterator()
        while (iterator.hasNext()) {
            val action = iterator.next()
            var pluginList = pluginMap[action]
            if (pluginList == null) {
                pluginList = ArrayList()
                pluginMap[action] = pluginList
            }
            pluginList.add(unityPlugin)
        }
        unityPlugin.onInitialize()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: InstantiationException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    }

    fun removePlugin(action: String?) {
        pluginMap.remove(action)
    }

    fun replacePlugin(unityPlugin: UnityPlugin) {
        val cocosEventFilter = UnityEventFilter()
        unityPlugin.onPrepare(cocosEventFilter)
        val iterator = cocosEventFilter.actionIterator()
        while (iterator.hasNext()) {
            val action = iterator.next()
            var pluginList = pluginMap[action]
            if (pluginList == null) {
                pluginList = ArrayList()
                pluginMap[action] = pluginList
            } else {
                pluginList.clear()
            }
            pluginList.add(unityPlugin)
        }
        unityPlugin.onInitialize()
    }

    fun getUnityPluginList(action: String?): List<UnityPlugin>? {
        return pluginMap[action]
    }

    fun hasAction(action: String?): Boolean {
        return pluginMap.containsKey(action)
    }

    fun clear() {
        pluginMap.clear()
    }

    interface PluginFactory {
        fun onPluginRegister(unityPluginManager: UnityPluginManager?)
        val tag: String
    }

    companion object {
        private val pluginFactoryMap: MutableMap<String, PluginFactory>? = HashMap()

        fun addPluginFactory(pluginFactory: PluginFactory?) {
            if (pluginFactory == null || TextUtils.isEmpty(pluginFactory.tag)) {
                return
            }
            pluginFactoryMap!![pluginFactory.tag] = pluginFactory
        }
    }

    init {
        if (pluginFactoryMap != null && pluginFactoryMap.isNotEmpty()) {
            val collection: Collection<PluginFactory> = pluginFactoryMap.values
            if (!collection.isEmpty()) {
                for (pluginFactory in collection) {
                    pluginFactory.onPluginRegister(this)
                }
            }
        }
    }
}