package com.tg.unitylibrary.plugin

import android.app.Activity
import android.os.Build
import android.slkmedia.mediastreamer.utils.NetWorkStateUtils
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.bx.soraka.Soraka
import com.tg.baselogin.cache.SpUser
import com.tg.core.utils.ScreenUtils
import com.tg.core.utils.TgTrackerUtils
import com.tg.unitylibrary.*
import com.tg.unitylibrary.container.UnityEventFilter
import com.tg.unitylibrary.container.UnityManager
import com.tg.unitylibrary.container.UnitySimplePlugin
import com.ypp.ui.util.ActivityUtils
import com.yupaopao.accountservice.AccountService
import com.yupaopao.android.h5container.uihelper.StatusBarHelper
import com.yupaopao.android.h5container.web.ResponseData
import com.yupaopao.lux.utils.toJSONString
import com.yupaopao.util.log.LogUtil
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class UnityGameBasePlugin : UnitySimplePlugin() {

    private var mAppId: String = ""//游戏的appId
    private var mRoomId: String = ""

    companion object {
        const val ACTION_GAME_GET_CONFIG = "game_getGameConfig"
        const val ACTION_GAME_ENTER_ROOM = "game_audioroom_enterRoom"
        const val ACTION_GAME_EXIT_ROOM = "game_audioroom_exitRoom"
        const val ACTION_GAME_EXIT_GAME = "game_audioroom_exitGame"
        const val ACTION_GAME_ENABLEMIC = "game_audioroom_enableMic"
        const val ACTION_GAME_ENABLEMUTE = "game_audioroom_enableMute"
        const val ACTION_MAPI = "network_mapi"
        const val ACTION_TRACK_EVENT = "log_trackEvent"
        const val ACTION_GAME_LOG_SORAKA = "game_log_soraka"
        const val ACTION_SET_NAVBARTYPE = "ui_setNavbarType"
        const val ACTION_GAME_SHOW_ACTION_BAR = "game_show_action_bar"
        const val ACTION_GAME_SWITCH_SCENE = "game_switchUnityView"
        const val ACTION_GAME_CLOSE_VIEW = "game_closeUnityView"
        const val ACTION_GAME_GET_GENDER = "game_hp_saved_gender"
        const val ACTION_GAME_END_LOADING = "game_end_loading"
    }

    override fun onPrepare(unityEventFilter: UnityEventFilter?) {
        unityEventFilter?.addAction(ACTION_GAME_GET_CONFIG)
        unityEventFilter?.addAction(ACTION_GAME_ENTER_ROOM)
        unityEventFilter?.addAction(ACTION_GAME_EXIT_GAME)
        unityEventFilter?.addAction(ACTION_GAME_EXIT_ROOM)
        unityEventFilter?.addAction(ACTION_GAME_ENABLEMIC)
        unityEventFilter?.addAction(ACTION_GAME_ENABLEMUTE)
        unityEventFilter?.addAction(ACTION_MAPI)
        unityEventFilter?.addAction(ACTION_TRACK_EVENT)
        unityEventFilter?.addAction(ACTION_GAME_LOG_SORAKA)
        unityEventFilter?.addAction(ACTION_SET_NAVBARTYPE)
        unityEventFilter?.addAction(ACTION_GAME_SHOW_ACTION_BAR)
        unityEventFilter?.addAction(ACTION_GAME_SWITCH_SCENE)
        unityEventFilter?.addAction(ACTION_GAME_CLOSE_VIEW)
        unityEventFilter?.addAction(ACTION_GAME_GET_GENDER)
        unityEventFilter?.addAction(ACTION_GAME_END_LOADING)
    }

    override fun handleEvent(
        context: Activity?,
        unityEvent: UnityEvent?,
        callback: IntergrationInterface?
    ) {
        when (unityEvent?.action) {
            ACTION_GAME_LOG_SORAKA -> {
                if (ActivityUtils.activityIsDestroyed(context)) {
                    return
                }
                val content = unityEvent.gameData?.getString("content") ?: ""
                val scene = unityEvent.gameData?.getString("scene") ?: ""
                val event = unityEvent.gameData?.getString("logEvent") ?: ""
                val reason = unityEvent.gameData?.getString("reason") ?: ""
                if (UnityManager.mInstance == null) {
                    Soraka.loge(scene, event, reason, content)
                } else {
                    UnityManager.mInstance?.soraka(scene, event, reason, content)
                }
            }

            ACTION_GAME_END_LOADING -> {
                if (context is MainUnityActivity) {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(600)
                        context.endLoading()
                    }
                } else {
                    EventBus.getDefault().post(MainSquareEndLoadingEvent())
                }
            }
            ACTION_GAME_SHOW_ACTION_BAR -> {
                val show = unityEvent.gameData?.getBoolean("show") ?: true
                if (show) {
                    context?.window?.addFlags(2048)
                } else {
                    context?.window?.clearFlags(2048)
                }
            }

            ACTION_SET_NAVBARTYPE -> {
                context?.apply {
                    val type = unityEvent.gameData?.getString("type") ?: "2"
                    GlobalScope.launch(Dispatchers.Main) {
                        if (type == "1") {
                            StatusBarHelper.setStatusBarDarkMode(context)
                        } else {
                            StatusBarHelper.setStatusBarLightMode(context)
                        }
                    }
                }
            }

            ACTION_GAME_GET_CONFIG -> {
                val jsonObject = JSONObject()
                jsonObject["gameBridgeVersion"] =
                    context?.packageManager?.getPackageInfo(context.packageName, 0)?.versionCode
                jsonObject["gameType"] = 2
                jsonObject["platform"] = 2
                jsonObject["ip"] = NetWorkStateUtils.GetHostIp()
                jsonObject["uid"] = UnityManager.unityAppInfoModule?.uid
                jsonObject["appVersion"] =
                    context?.packageManager?.getPackageInfo(context.packageName, 0)?.versionName
                jsonObject["deviceId"] = UnityManager.unityAppInfoModule?.deviceId
                jsonObject["accessToken"] = AccountService.getInstance().accessToken
                jsonObject["networkType"] = UnityManager.unityAppInfoModule?.netType
                jsonObject["systemVersion"] = Build.VERSION.RELEASE
                jsonObject["bundleId"] = UnityManager.unityAppInfoModule?.bundleId
                jsonObject["machine"] = Build.MODEL
                jsonObject["channel"] = UnityManager.unityAppInfoModule?.appChannel
                jsonObject["yppenv"] = UnityManager.unityAppInfoModule?.apiEnvironment
                val navigationBarHeight =
                    ScreenUtils.px2dp(context, ScreenUtils.getNavigationBarHeight(context))
                val statusBarHeight =
                    ScreenUtils.px2dp(context, ScreenUtils.getStatusBarHeight(context))
                val screenHeight = ScreenUtils.px2dp(context, ScreenUtils.getScreenHeight(context))
                val screenWidth = ScreenUtils.px2dp(context, ScreenUtils.getScreenWidth(context))
                jsonObject["statusbarHeight"] = statusBarHeight
                jsonObject["navbarHeight"] = navigationBarHeight
                jsonObject["screenHeight"] = screenHeight
                jsonObject["screenWidth"] = screenWidth
                unityEvent.gameData = jsonObject
                callback?.NativeCallback(JSON.toJSONString(unityEvent))
            }

            ACTION_GAME_ENTER_ROOM -> {
                mRoomId = unityEvent.gameData?.getString("roomId") ?: return
                mAppId = unityEvent.gameData?.getString("appId") ?: ""
                val product = unityEvent.gameData?.getString("product") ?: "GAME"
                val enablePullAllStream = unityEvent.gameData?.getBoolean("enablePullAllStream")
                    ?: false
                val enableStream = unityEvent.gameData?.getBoolean("enableStream") ?: false
                val enableAudioSonic = unityEvent.gameData?.getBoolean("enableAudioSonic")
                    ?: false
                context?.let { it ->
                    if (ActivityUtils.activityIsDestroyed(it)) {
                        return
                    }
                    UnityManager.mInstance?.leaveAudioRoom(mRoomId, unityEvent)
                    UnityManager.mInstance?.enterRoom(
                        mRoomId,
                        product,
                        enablePullAllStream,
                        enableStream,
                        enableAudioSonic,
                        unityEvent
                    )
                }
            }

            ACTION_GAME_ENABLEMIC -> {
                //1 开麦 2 关麦
                val status = unityEvent.gameData?.getIntValue("status") ?: 1
                UnityManager.mInstance?.switchMic(status, mRoomId, unityEvent)
            }

            ACTION_GAME_EXIT_GAME -> {
                UnityManager.mInstance?.exitGame(mRoomId, unityEvent)
                context?.finish()
            }
            ACTION_GAME_EXIT_ROOM -> {
                UnityManager.mInstance?.leaveAudioRoom(mRoomId, unityEvent)
            }

            ACTION_GAME_ENABLEMUTE -> {
                val uids = JSONObject.parseArray(
                    (unityEvent.gameData?.getJSONArray("uids"))?.toString(),
                    String::class.java
                )
                //1 静音, 2 取消静音
                val status = unityEvent.gameData?.getIntValue("status") ?: 1
                UnityManager.mInstance?.muteUids(uids, status, mRoomId, unityEvent)
            }

            ACTION_TRACK_EVENT -> {
                val uploadMode = unityEvent.gameData?.getIntValue("uploadMode")
                val dataObject = unityEvent.gameData?.getJSONObject("data")

                try {
                    if (UnityManager.mInstance == null) {
                        GlobalScope.launch(Dispatchers.Main) {
                            dataObject?.let {
                                TgTrackerUtils.postPassThroughEvent(
                                    filterJson(dataObject.toJSONString()),
                                    uploadMode == 1
                                )
                            }
                        }
                    } else {
                        UnityManager.mInstance?.tracker(
                            dataObject.toJSONString(),
                            (uploadMode == 1)
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (error: Error) {

                } finally {
                    baseMessage(unityEvent, callback, null, ResponseData.SUCCESS)
                }
            }

            ACTION_MAPI -> {
                val url = unityEvent.gameData?.getString("url")
                val method = unityEvent.gameData?.getString("method")
                val params = unityEvent.gameData?.getString("params")
                val headers = unityEvent.gameData?.getString("headers")
                if (TextUtils.isEmpty(url)) {
                    return
                }
                if (TextUtils.equals(method, "GET")) {
                    var headersMap: HashMap<String?, String?>? = HashMap()
                    if (!TextUtils.isEmpty(headers)) {
                        try {
                            headersMap = JSON.parseObject<HashMap<*, *>>(
                                headers,
                                HashMap::class.java
                            ) as HashMap<String?, String?>?
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                            return
                        }
                    }
                    UnityApi.getGateWayRequest(url, headersMap)?.subscribe(object :
                        DisposableSubscriber<JSONObject?>() {
                        override fun onNext(o: JSONObject?) {
                            if (context == null || context.isFinishing) {
                                return
                            }
                            unityEvent.gameData = o
                            callback?.NativeCallback(JSON.toJSONString(unityEvent))
                        }

                        override fun onError(t: Throwable) {
                            if (context == null || context.isFinishing) {
                                return
                            }
                            baseMessage(
                                unityEvent,
                                callback,
                                ResponseData.NETWORK_PROMPT,
                                ResponseData.NETWORK_ERROR
                            )
                        }

                        override fun onComplete() {}
                    })
                } else {
                    var paramsMap: HashMap<String?, Any?>? = HashMap()
                    if (!TextUtils.isEmpty(params)) {
                        try {
                            paramsMap = JSON.parseObject<HashMap<*, *>>(
                                params,
                                HashMap::class.java
                            ) as HashMap<String?, Any?>?
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                            return
                        }
                    }
                    var headersMap: HashMap<String?, String?>? = HashMap()
                    if (!TextUtils.isEmpty(headers)) {
                        try {
                            headersMap = JSON.parseObject<HashMap<*, *>>(
                                headers,
                                HashMap::class.java
                            ) as HashMap<String?, String?>?
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                            return
                        }
                    }
                    UnityApi.postGateWayRequest(url, paramsMap, headersMap)?.subscribe(object :
                        DisposableSubscriber<JSONObject?>() {
                        override fun onNext(o: JSONObject?) {
                            if (context == null || context.isFinishing) {
                                return
                            }
                            unityEvent.gameData = o
                            callback?.NativeCallback(JSON.toJSONString(unityEvent))
                        }

                        override fun onError(t: Throwable) {
                            if (context == null || context.isFinishing) {
                                return
                            }
                            baseMessage(
                                unityEvent,
                                callback,
                                ResponseData.NETWORK_PROMPT,
                                ResponseData.NETWORK_ERROR
                            )
                        }

                        override fun onComplete() {
                            Log.d("helei", "onComplete")
                        }
                    })
                }
            }

            ACTION_GAME_SWITCH_SCENE -> {
                val viewId = unityEvent.gameData?.getIntValue("ViewId") ?: 1
                val gameEventId = unityEvent.gameEventId ?: ""

                LogUtil.i("Unity--- ACTION_GAME_SWITCH_SCENE ViewId = $viewId")
                LogUtil.i("Unity--- ACTION_GAME_SWITCH_SCENE gameEventId = $gameEventId")

                EventBus.getDefault().post(UnitySwitchSceneEvent(viewId, gameEventId))
                if (viewId == 51) {
                    EventBus.getDefault().post(MainViewSetVisibilityEvent(false))
                }
            }

            ACTION_GAME_CLOSE_VIEW -> {
                val resultJson = JSONObject()
                resultJson["code"] = ResponseData.SUCCESS
                unityEvent.gameData = resultJson
                callback?.NativeCallback(JSON.toJSONString(unityEvent))
                EventBus.getDefault().post(MainViewSetVisibilityEvent(true))
            }

            ACTION_GAME_GET_GENDER -> {
                val resultJson = JSONObject()
                resultJson["gender"] = SpUser.getInstance().userGender.toInt()
                unityEvent.gameData = resultJson
                callback?.NativeCallback(JSON.toJSONString(unityEvent))
            }
        }
    }

    private fun baseMessage(
        unityEvent: UnityEvent,
        callback: IntergrationInterface?,
        msg: String?,
        code: Int
    ) {
        unityEvent.gameData = ResponseData(
            code,
            msg,
            null
        )
        callback?.NativeCallback(JSON.toJSONString(unityEvent))
    }

    private fun filterJson(data: String): Map<String, String>? {
        val dataObject = JSONObject.parseObject(data)
        val hashMap = java.util.HashMap<String, String>()
        if (dataObject == null) {
            return hashMap
        }
        for (key in dataObject.innerMap.keys) {
            if (dataObject[key] is String) {
                hashMap[key] = dataObject.getString(key)
            }
        }
        return hashMap
    }
}