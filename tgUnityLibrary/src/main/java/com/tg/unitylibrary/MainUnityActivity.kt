package com.tg.unitylibrary

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.GsonBuilder
import com.tg.baselogin.manager.UpdateManager
import com.tg.baselogin.user.model.UpdateModel
import com.tg.ui.dialog.TgBaseDialog
import com.tg.unitylibrary.container.UnityManager
import com.tg.unitylibrary.container.UnityPluginFactory
import com.tg.unitylibrary.container.UnityPluginManager
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerActivity
import com.ypp.net.ApiServiceManager
import com.ypp.net.lift.ResultSubscriber
import com.ypp.net.lift.RxSchedulers
import com.ypp.ui.widget.yppmageview.YppImageView
import com.yupaopao.animation.apng.APNGDrawable
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONException
import org.json.JSONObject
import kotlin.system.exitProcess


@Route(path = "/unityGame/open")
open class MainUnityActivity : UnityPlayerActivity() {
    @Autowired(name = "viewId")
    @JvmField
    var viewId: Int = -1

    @Autowired(name = "errorMsg")
    @JvmField
    var mErrorMsg: String = ""

    private val mLoadingView: View by lazy {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.tgui_state_tg_loading_layout, mUnityPlayer, false)
        val loading = view?.findViewById<YppImageView>(R.id.loading_image)
        val tgLoadingContainer = view?.findViewById<LinearLayout>(R.id.tgLoadingContainer)
        tgLoadingContainer?.setBackgroundColor(Color.parseColor("#ffffff"))
        val apngDrawable = APNGDrawable.fromResource(this, R.raw.tgui_state_loading)
        loading?.setImageDrawable(apngDrawable)
        apngDrawable.start()
        view
    }
    private var mIsRemove = false
    private var remoteService: IUnityPluginContext? = null
    private val mDeathRecipient: IBinder.DeathRecipient = IBinder.DeathRecipient { serviceDead() }
    private var connected = false
    private var mUnityPluginManager: UnityPluginManager? = null
    private var mPluginFactory: UnityPluginManager.PluginFactory? = null
    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()
    private var mUnityCallback: IntergrationInterface? = null

    private val mCallBack = object : IAIDLCallBack.Stub() {
        override fun onEvent(type: String?, args: String?) {
            if ("unity_page_close" == type) {
                UnityManager.dispatchEvent(
                    this@MainUnityActivity, UnityEvent(
                        "page_close", "", "", null
                    ), mUnityCallback
                )
            } else if ("unity_clear_register" == type) {
                val jsonObject = com.alibaba.fastjson.JSONObject()
                jsonObject["clearCache"] = true
                val unityEventTemp = UnityEvent(
                    "",
                    "NativeBridge.Logics.ClearRegisterDataBridgeResponse",
                    "",
                    jsonObject
                )
                mUnityCallback?.NativeCallback(GsonBuilder().create().toJson(unityEventTemp))
            }
        }

        override fun sendBridgeResult(unityEvent: UnityEvent?) {
            UnityManager.sendBridgeResult(unityEvent)
        }
    }


    private val aidlServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i("12helei","5")
            remoteService = IUnityPluginContext.Stub.asInterface(service)
            try {
                if (remoteService != null) {
                    service.linkToDeath(mDeathRecipient, 0)
                    remoteService!!.setAIDLCallBack(mCallBack)
                    UnityManager.mInstance = remoteService
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            connected = true

        }

        override fun onServiceDisconnected(name: ComponentName) {
            remoteService = null
            connected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ARouter.init(this.application)
        ARouter.getInstance().inject(this)
        super.onCreate(savedInstanceState)
        mUnityPlayer.addView(mLoadingView)
        bind()
        mUnityPluginManager = UnityPluginManager()
        mPluginFactory = UnityPluginFactory()
        mPluginFactory?.onPluginRegister(mUnityPluginManager)
        UnityManager.initGame(mUnityPluginManager)
        sendMessage(viewId)
        checkErrorApi()
        val subscriber: ResultSubscriber<UpdateModel?> = ApiServiceManager.getInstance()
            .obtainService(VersionUpdateApi::class.java).requestUpdateInfo()
            .compose(RxSchedulers.ioToMain())
            .subscribeWith(object : ResultSubscriber<UpdateModel?>() {
                override fun onSuccesses(model: UpdateModel?) {
                    super.onSuccesses(model)
                    UpdateManager.checkUpdate(model, this@MainUnityActivity)
                }
            })
        compositeDisposable?.add(subscriber)
    }

    private fun bind() {
        val intent  = Intent(this, UnityPluginService::class.java)
        try {
            application.bindService(
                intent,
                aidlServiceConnection,
                BIND_AUTO_CREATE
            )
        } catch (e: Exception) {
            Log.i("12helei","01"+e.toString())
        }
    }

    private fun checkErrorApi() {
        if (!TextUtils.isEmpty(mErrorMsg)) {
            val tgBaseDialog = TgBaseDialog()
            tgBaseDialog.setSureText("确定")
            tgBaseDialog.setTitle("登陆失效")
            tgBaseDialog.setContent("你长时间不在线,请重新登录")
            tgBaseDialog.show(supportFragmentManager)
        }
    }

    private fun sendMessage(viewId: Int) {
        val obj: JSONObject?
        try {
            obj = JSONObject()
            obj.put("gameEvent", "NativeBridge.Logics.SwitchViewBridgeResponse")
            obj.put("gameEventId", "")
            val params = JSONObject()
            params.put("ViewId", viewId)
            obj.put("gameData", params)
            UnityPlayer.UnitySendMessage(
                "NativeBridge",
                "ReceiveDataFromPlatform",
                obj.toString()
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
        if (connected) {
            try {
                UnityManager.mInstance = null
                remoteService?.setAIDLCallBack(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            unbindService(aidlServiceConnection)
        }
        exitProcess(0)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sendMessage(intent.getIntExtra("viewId", -1))
    }

    private fun serviceDead() {
        if (remoteService != null) {
            remoteService?.asBinder()?.unlinkToDeath(mDeathRecipient, 0)
            bind()
        }
    }

    fun dispatchUnityEvent(json: String, callback: IntergrationInterface?) {
        mUnityCallback = callback
        val obj = JSONObject(json)
        val action = obj.getString("action")
        val gameEvent = obj.getString("gameEvent")
        val gameEventId = obj.getString("gameEventId")
        var data: String? = null
        if (obj.has("data")) {
            data = obj.getString("data")
        }
        val unityEvent = UnityEvent(
            action, gameEvent, gameEventId, com.alibaba.fastjson.JSONObject.parseObject(
                data
            )
        )
        UnityManager.dispatchEvent(this, unityEvent, callback)
    }

    open fun endLoading() {
        if (!mIsRemove) {
            mUnityPlayer?.removeView(mLoadingView)
            mIsRemove = true
        }
    }
}