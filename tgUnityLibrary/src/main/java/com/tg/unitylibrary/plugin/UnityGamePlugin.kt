package com.tg.unitylibrary.plugin

import android.app.Activity
import android.app.Service
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.bx.soraka.Soraka
import com.google.gson.GsonBuilder
import com.tg.baselogin.cache.SpUser
import com.tg.baselogin.manager.UserManager
import com.tg.unitylibrary.IntergrationInterface
import com.tg.unitylibrary.MainTopViewSetVisibilityEvent
import com.tg.unitylibrary.UnityEvent
import com.tg.unitylibrary.container.UnityEventFilter
import com.tg.unitylibrary.container.UnityManager
import com.tg.unitylibrary.container.UnitySimplePlugin
import com.tg.core.event.ShowBindDialogEvent
import com.ypp.ui.util.ActivityUtils
import com.yupaopao.android.h5container.web.ResponseData
import com.yupaopao.fileupload.UploadResultSubscriber
import com.yupaopao.fileupload.YppUploadManager
import com.yupaopao.fileupload.constant.UploadBusinessType
import com.yupaopao.fileupload.repository.model.UploadResult
import com.yupaopao.util.base.rx.RxSchedulers
import org.greenrobot.eventbus.EventBus

class UnityGamePlugin : UnitySimplePlugin() {


    companion object {
        const val ACTION_GAME_APP_UPLOADAVATARINFO = "game_app_uploadAvatarInfo"
        const val ACTION_IMAGE_UPLOADIMAGE = "image_uploadImage"
        const val ACTION_GAME_APP_LOGIN = "game_app_login"
        const val ACTION_PAGE_CLOSE = "page_close"
        const val ACTION_GAME_CHECK_MOBILE_BIND = "game_checkMobileBind"
        const val GAME_IMPACT_FEEDBACK = "game_impactFeedback"
        const val ACTION_GAME_GLOBAL_GENDER_CHANGED = "game_global_genderHasChanged"
        const val ACTION_GAME_APP_SAVEAVATARCOMPLETIONE = "game_app_saveAvatarCompletion"
        const val ACTION_GAME_NATIVE_OPEN_SCHEME = "game_native_open_scheme"
        const val ACTION_GAME_HOME_SQUARE_SET_VIEW_VISIBILITY = "game_home_square_setViewVisibility"

    }

    override fun onPrepare(unityEventFilter: UnityEventFilter?) {
        unityEventFilter?.addAction(ACTION_GAME_APP_UPLOADAVATARINFO)
        unityEventFilter?.addAction(ACTION_GAME_CHECK_MOBILE_BIND)
        unityEventFilter?.addAction(ACTION_IMAGE_UPLOADIMAGE)
        unityEventFilter?.addAction(ACTION_GAME_APP_LOGIN)
        unityEventFilter?.addAction(ACTION_PAGE_CLOSE)
        unityEventFilter?.addAction(ACTION_GAME_APP_SAVEAVATARCOMPLETIONE)
        unityEventFilter?.addAction(ACTION_GAME_NATIVE_OPEN_SCHEME)
        unityEventFilter?.addAction(ACTION_GAME_HOME_SQUARE_SET_VIEW_VISIBILITY)
        unityEventFilter?.addAction(ACTION_GAME_GLOBAL_GENDER_CHANGED)
        unityEventFilter?.addAction(GAME_IMPACT_FEEDBACK)

    }

    override fun handleEvent(
        context: Activity?,
        unityEvent: UnityEvent?,
        callback: IntergrationInterface?
    ) {
        when (unityEvent?.action) {
            ACTION_GAME_CHECK_MOBILE_BIND -> {
                if (ActivityUtils.activityIsDestroyed(context)) {
                    return
                }
                if (UserManager.getInstance().isPhoneBind) {
                    callback?.NativeCallback(JSON.toJSONString(unityEvent))
                } else {
                    EventBus.getDefault().post(ShowBindDialogEvent())
                }
            }

            GAME_IMPACT_FEEDBACK -> {
                context?.let { it ->
                    val vib = context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                    if (!vib.hasVibrator()) return
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(
                            VibrationEffect.createOneShot(
                                200,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        vib.vibrate(200)
                    }
                }
            }

            ACTION_GAME_GLOBAL_GENDER_CHANGED -> {
                context?.let { it ->
                    val gender = unityEvent.gameData?.getString("gender") ?: ""
                    if (!TextUtils.isEmpty(gender)) {
                        SpUser.getInstance().updateUserGender(gender)
                    }

                }
            }

            ACTION_GAME_APP_SAVEAVATARCOMPLETIONE -> {
                context?.let { it ->
                    if (ActivityUtils.activityIsDestroyed(it)) {
                        return
                    }
                    val status = unityEvent.gameData?.getBoolean("status") ?: false
                    if (status) {
                        UnityManager.mInstance?.closeDialog()
                    }
                }
            }

            ACTION_GAME_APP_UPLOADAVATARINFO -> {
                context?.let { it ->
                    if (ActivityUtils.activityIsDestroyed(it)) {
                        return
                    }
                    UnityManager.mInstance?.goLogin("1", unityEvent)
                }
            }
            ACTION_GAME_APP_LOGIN -> {
                Log.i("helei",UnityManager.mInstance.toString())
                UnityManager.mInstance?.goLogin("2", unityEvent)
            }

            ACTION_IMAGE_UPLOADIMAGE -> {
                val data = unityEvent.gameData?.getString("data")
                val byteArray =
                    Base64.decode(data?.replace("data:image/png;base64,", ""), Base64.DEFAULT)
                YppUploadManager.uploadImage(UploadBusinessType.USER, byteArray)
                    .compose(RxSchedulers.ioToMain())
                    .subscribeWith(object : UploadResultSubscriber() {
                        override fun onSingleFileUploadSuccess(result: UploadResult?) {
                            val url = result?.url
                            val resultJson = JSONObject()
                            resultJson["url"] = url
                            resultJson["code"] = ResponseData.SUCCESS
                            unityEvent.gameData = resultJson
                            callback?.NativeCallback(GsonBuilder().create().toJson(unityEvent))
                        }

                        override fun onError(t: Throwable?) {
                            baseMessage(
                                unityEvent,
                                callback,
                                ResponseData.NETWORK_PROMPT,
                                ResponseData.NETWORK_ERROR
                            )
                        }

                        override fun onSingleFileUploadFailure(result: UploadResult?) {
                            baseMessage(
                                unityEvent,
                                callback,
                                ResponseData.NETWORK_PROMPT,
                                ResponseData.NETWORK_ERROR
                            )
                        }
                    })
            }

            ACTION_PAGE_CLOSE -> {
                context?.finish()
            }

            ACTION_GAME_NATIVE_OPEN_SCHEME -> {
                val url = unityEvent?.gameData?.getString("url") ?: ""
                ARouter.getInstance().build(url).navigation(context)
            }

            ACTION_GAME_HOME_SQUARE_SET_VIEW_VISIBILITY -> {
                val visible = unityEvent?.gameData?.getBoolean("visible") ?: true
                EventBus.getDefault().post(MainTopViewSetVisibilityEvent(visible))
            }
        }
    }


    private fun baseMessage(
        unityEvent: UnityEvent,
        callback: IntergrationInterface?,
        msg: String?,
        code: Int
    ) {
        Soraka.loge("avatar上传图片失败:", "avatarData", "error: $" + msg)
        unityEvent.gameData = ResponseData(
            code,
            msg,
            null
        )
        callback?.NativeCallback(GsonBuilder().create().toJson(unityEvent))
    }
}