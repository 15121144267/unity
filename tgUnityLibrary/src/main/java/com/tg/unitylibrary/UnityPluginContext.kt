package com.tg.unitylibrary

import android.os.DeadObjectException
import android.text.TextUtils
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSONObject
import com.bx.soraka.Soraka
import com.tg.baselogin.cache.SpCommon
import com.tg.baselogin.manager.UserLoginManager
import com.tg.baselogin.manager.UserManager
import com.tg.core.model.AvatarEditModel
import com.tg.core.onekey.OneKeyLoginHelper
import com.tg.core.utils.TgTrackerUtils
import com.tg.unitylibrary.container.UnityManager
import com.yupaopao.audioroom.sona.RoomEventObserver
import com.yupaopao.util.log.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.*


/**
 * @author Created by helei
 * @data 26.10.21
 * Email:helei19910210@163.com
 * Description:
 */
class UnityPluginContext : IUnityPluginContext.Stub() {
    private var aidlCallBack: IAIDLCallBack? = null
    override fun closeDialog() {
        EventBus.getDefault().post(AvatarEditModel())
    }

    override fun soraka(scene: String?, event: String?, reason: String?, content: String?) {
        if (!TextUtils.isEmpty(content)) {
            Soraka.loge(scene!!, event!!, reason!!, content!!)
        }
    }

    override fun tracker(data: String?, upload: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            TgTrackerUtils.postPassThroughEvent(filterJson(data!!), upload)
        }
    }

    override fun goLogin(type: String?, unityEvent: UnityEvent?) {
        if (type == "1") {
            UserManager.getInstance().setUserInfoData(unityEvent?.gameData)
            if (SpCommon.getInstance().commonSp.decodeString(
                    "loginType",
                    "PHONE"
                ) != "PHONE" && UserManager.getInstance().loginResponseModel != null
            ) {
                UserLoginManager.addUserLoginInterface(object :
                    UserLoginManager.OnUserLoginInterface {
                    override fun onComplete() {
                        onEvent("unity_clear_register", "")
                    }

                })
                UserLoginManager.sendUserInfo()
            } else {
                jumpLogin(type)
            }

        } else {
            jumpLogin(type)
        }
    }

    private fun jumpLogin(type: String?) {
        if (OneKeyLoginHelper.isInitSuccess && OneKeyLoginHelper.isHasPreLogin) {
            if (OneKeyLoginHelper.isPreLoginSuccess) {
                if (type == "1") {
                    ARouter.getInstance().build("/simNew/enter").greenChannel().navigation()
                } else {
                    ARouter.getInstance().build("/sim/enter").greenChannel().navigation()
                }
            } else {
                goToLogin(type)
            }
        } else {
            goToLogin(type)
        }
        OneKeyLoginHelper.clear()
    }

    private fun goToLogin(type: String?) {
        if (type == "1") {
            ARouter.getInstance().build("/loginNew/enter").greenChannel().navigation()
        } else {
            ARouter.getInstance().build("/login/enter").greenChannel().navigation()
        }

    }

    override fun enterRoom(
        roomId: String,
        product: String,
        enablePullAllStream: Boolean,
        enableStream: Boolean,
        enableAudioSonic: Boolean,
        unityEvent: UnityEvent?
    ) {
        UnityManager.enterRoom(
            roomId,
            product,
            enablePullAllStream,
            enableStream,
            enableAudioSonic,
            unityEvent,
            object : UnityManager.RoomCallback {
                override fun onSuccess(roomId: String?) {
                    val jsonObject = JSONObject()
                    jsonObject["roomId"] = roomId
                    jsonObject["code"] = 0
                    unityEvent?.gameData = jsonObject
                    sendBridgeResult(unityEvent)
                }

                override fun onFailed(code: Int, reason: String?) {
                    val jsonObject = JSONObject()
                    jsonObject["roomId"] = roomId
                    jsonObject["code"] = code
                    jsonObject["reason"] = reason ?: "enterRoom failed"
                    unityEvent?.gameData = jsonObject
                    sendBridgeResult(unityEvent)
                }

            })
    }


    override fun speak(status: Int, roomId: String?, unityEvent: UnityEvent?) {
    }

    override fun listen(status: Int, roomId: String?, unityEvent: UnityEvent?) {
    }

    override fun listenStream(
        uids: MutableList<String>?,
        roomId: String?,
        unityEvent: UnityEvent?
    ) {
    }

    override fun switchMic(status: Int, roomId: String?, unityEvent: UnityEvent?) {
        UnityManager.switchMic(status == 1, object : UnityManager.AudioRoomCallback {
            override fun onSuccess() {
                unityEvent?.let {
                    val jsonObject = JSONObject()
                    jsonObject["roomId"] = roomId
                    jsonObject["status"] = status
                    jsonObject["code"] = 0
                    unityEvent.gameData = jsonObject
                    sendBridgeResult(it)
                }
            }

            override fun onFailed(code: Int, reason: String?) {
                unityEvent?.let {
                    val jsonObject = JSONObject()
                    jsonObject["roomId"] = roomId
                    jsonObject["code"] = code
                    jsonObject["status"] = status
                    jsonObject["reason"] = reason ?: "enableMic failed"
                    unityEvent.gameData = jsonObject
                    sendBridgeResult(it)
                }
            }
        })
    }

    override fun leaveAudioRoom(roomId: String?, unityEvent: UnityEvent?) {
        UnityManager.leaveRoom(object : UnityManager.AudioRoomCallback {
            override fun onFailed(code: Int, reason: String?) {
                unityEvent?.let {
                    val jsonObject = JSONObject()
                    jsonObject["roomId"] = roomId
                    jsonObject["code"] = code
                    jsonObject["reason"] = reason ?: "leaveAudioRoom failed"
                    unityEvent.gameData = jsonObject
                    sendBridgeResult(unityEvent)
                }
            }

            override fun onSuccess() {
                unityEvent?.let {
                    val jsonObject = JSONObject()
                    jsonObject["roomId"] = roomId
                    jsonObject["code"] = 0
                    unityEvent.gameData = jsonObject
                    sendBridgeResult(unityEvent)
                }
            }
        })
    }

    override fun exitGame(roomId: String?, unityEvent: UnityEvent?) {
        UnityManager
            .exitGame(object : UnityManager.AudioRoomCallback {
                override fun onSuccess() {
                    observerRoom()
                    unityEvent?.let {
                        val jsonObject = JSONObject()
                        jsonObject["roomId"] = roomId
                        jsonObject["code"] = 0
                        unityEvent.gameData = jsonObject
                        sendBridgeResult(it)
                    }
                }

                override fun onFailed(code: Int, reason: String?) {
                    unityEvent?.let {
                        val jsonObject = JSONObject()
                        jsonObject["roomId"] = roomId
                        jsonObject["code"] = code
                        jsonObject["reason"] = reason ?: "leaveRoom failed"
                        unityEvent.gameData = jsonObject
                        sendBridgeResult(it)
                    }
                }
            })
    }

    override fun muteUid(uid: String?, status: Int, roomId: String?, unityEvent: UnityEvent?) {
        if (uid.isNullOrBlank() || uid.isNullOrEmpty())
            return
        muteUids(mutableListOf<String>().apply {
            add(uid)
        }, status, roomId, unityEvent)
    }

    override fun muteUids(
        uids: MutableList<String>?,
        status: Int,
        mRoomId: String?,
        unityEvent: UnityEvent?
    ) {
        val streamIds = UnityManager.currentStreamIds()
        uids?.forEach {
            streamIds?.forEach { streamId ->
                val uid = getUidFromStreamId(streamId)
                if (uid.equals(it)) {
                    UnityManager.silent(streamId,
                        status == 1,
                        object : UnityManager.AudioRoomCallback {
                            override fun onSuccess() {
                                unityEvent?.let {
                                    val jsonObject = JSONObject()
                                    jsonObject["roomId"] = mRoomId
                                    jsonObject["code"] = 0
                                    unityEvent.gameData = jsonObject
                                    sendBridgeResult(unityEvent)
                                }
                            }

                            override fun onFailed(code: Int, reason: String?) {
                                unityEvent?.let {
                                    val jsonObject = JSONObject()
                                    jsonObject["roomId"] = mRoomId
                                    jsonObject["code"] = code
                                    jsonObject["reason"] = reason ?: "enableMute failed"
                                    unityEvent.gameData = jsonObject
                                    sendBridgeResult(unityEvent)
                                }
                            }
                        })
                }
            }
        }
    }


    override fun setAIDLCallBack(callback: IAIDLCallBack?) {
        aidlCallBack = callback
        UnityManager.aidlCallBack = callback
    }

    private fun sendBridgeResult(unityEvent: UnityEvent?) {
        try {
            if (aidlCallBack != null && aidlCallBack!!.asBinder().isBinderAlive) {
                aidlCallBack!!.sendBridgeResult(unityEvent)
            }
        } catch (e: DeadObjectException) {
            e.printStackTrace()
        }
    }


    private fun onEvent(type: String, args: String?) {
        try {
            if (aidlCallBack != null && aidlCallBack!!.asBinder().isBinderAlive) {
                aidlCallBack!!.onEvent(type, args)
            }
        } catch (e: DeadObjectException) {
            e.printStackTrace()
        }
    }

    private fun observerRoom() {
        UnityManager.roomEventObserver = object : RoomEventObserver {
            override fun onReceiveEvent(i: Int) {}
            override fun onReceiveMessage(s: String?) {
                LogUtil.d("UnityBridgeService", "onReceiveMessage-----$s")
                onEvent("onYppAudioRoomReceiveMessage", s)
            }

            override fun onReceiveReward(s: String) {
                LogUtil.d("UnityBridgeService", "onReceiveReward-----$s")
                val jsonObject = JSONObject()
                jsonObject["data"] = JSONObject.parseObject(s)
                onEvent("onYppAudioRoomReceiveReward", jsonObject.toJSONString())
            }

            override fun onReceiveSonic(s: String?) {
                onEvent("onYppAudioRoomReceiveSonic", s)
            }
        }
    }


    private fun getUidFromStreamId(streamId: String): String? {
        var uid: String? = null
        if (!TextUtils.isEmpty(streamId)) {
            val endPos = streamId.lastIndexOf('_')
            if (endPos < 0) {
                return uid
            }
            val startPos = streamId.substring(0, endPos).lastIndexOf('_')
            if (startPos < 0 || startPos + 1 > endPos) {
                return uid
            }
            uid = streamId.substring(startPos + 1, endPos)
        }
        return uid
    }

    private fun filterJson(data: String): Map<String, String>? {
        val dataObject = JSONObject.parseObject(data)
        val hashMap = HashMap<String, String>()
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