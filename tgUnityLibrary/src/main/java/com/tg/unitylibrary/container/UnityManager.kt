package com.tg.unitylibrary.container

import android.app.Activity
import android.app.Application
import android.text.TextUtils
import com.google.gson.GsonBuilder
import com.tg.unitylibrary.IAIDLCallBack
import com.tg.unitylibrary.IUnityPluginContext
import com.tg.unitylibrary.IntergrationInterface
import com.tg.unitylibrary.Module.UnityAppInfoModule
import com.tg.unitylibrary.UnityEvent
import com.yupaopao.android.audioservice.AudioSessionManager
import com.yupaopao.android.audioservice.IAudioSession
import com.yupaopao.audioroom.sona.AudioRoomManager
import com.yupaopao.audioroom.sona.AudioRoomManager.Companion.ROOM_ERROR
import com.yupaopao.audioroom.sona.RoomEventObserver
import com.yupaopao.sona.SonaRoom
import com.yupaopao.sona.SonaRoomCallback
import com.yupaopao.sona.plugin.AudioNoiseSuppressionPlugin
import com.yupaopao.sona.plugin.GameAudioPlugin
import com.yupaopao.sona.plugin.PluginCallback
import com.yupaopao.sona.plugin.TgAudioVoiceMixPlugin
import com.yupaopao.sona.plugin.config.AudioConfig
import com.yupaopao.sona.plugin.entity.SoundLevelInfoEntity
import com.yupaopao.sona.plugin.entity.SpeakEntity
import com.yupaopao.sona.plugin.observer.GameAudioPluginObserver
import com.yupaopao.sona.util.SonaLogger

object UnityManager : IAudioSession{
    var application: Application? = null
    var unityAppInfoModule: UnityAppInfoModule? = null
    var pluginManager: UnityPluginManager? = null
    var mInstance: IUnityPluginContext? = null
    var sonaRoom: SonaRoom? = null
    var sonaRoomId: String? = null
    var aidlCallBack: IAIDLCallBack? = null
    var mUnityCallback: IntergrationInterface? =null
    private var enterRoomProcessing: Boolean = false
    var roomEventObserver: RoomEventObserver? = null
    var enableCaptureSound: Boolean = false
    private var sonicStatus: Boolean = true
    private var soundLevelList: MutableList<SoundLevelInfoEntity>? = null
    private var volume: Int = 50

    fun init(app: Application?, builder: Builder?) {
        application = app
        if (builder != null) {
            UnityPluginManager.addPluginFactory(builder.pluginFactory)
            unityAppInfoModule = builder.unityAppInfoModule
        }
    }

    fun initGame(pluginManager: UnityPluginManager?) {
        this.pluginManager = pluginManager
    }

    val appContext: Application?
        get() {
            if (application == null) {
                throw NullPointerException()
            }
            return application
        }

    class Builder {
        var pluginFactory: UnityPluginManager.PluginFactory? = null

        var unityAppInfoModule: UnityAppInfoModule? = null

        fun setPluginFactory(pluginFactory: UnityPluginManager.PluginFactory?): Builder {
            this.pluginFactory = pluginFactory
            return this
        }

        fun setUnityAppInfoModule(unityAppInfoModule: UnityAppInfoModule?): Builder {
            this.unityAppInfoModule = unityAppInfoModule
            return this
        }
    }

    fun dispatchEvent(
        context: Activity?,
        unityEvent: UnityEvent?,
        callback: IntergrationInterface?
    ) {
        if (unityEvent == null || TextUtils.isEmpty(unityEvent.action)) {
            return
        }
        mUnityCallback = callback
        val pluginList: List<UnityPlugin>? = pluginManager?.getUnityPluginList(unityEvent.action)
        pluginList?.apply {
            for (i in indices) {
                pluginList[i].handleEvent(context, unityEvent, callback)
            }
        }
    }

    fun enterRoom(
        roomId: String,
        product: String,
        enablePullAllStream: Boolean,
        enableStream: Boolean,
        enableAudioSonic: Boolean,
        unityEvent: UnityEvent?,
        callback: RoomCallback
    ) {
        sonaRoom = null
        sonaRoomId = null
        sonaRoom = SonaRoom()
        registerPlugins()
        enterRoomProcessing = true
        sonicStatus = enableAudioSonic
        sonaRoom?.enterRoom(roomId, product, "", null, object : SonaRoomCallback {
            override fun onSuccess(roomId: String?) {
                sonaRoomId = roomId
                sonaRoom?.getPlugin(GameAudioPlugin::class.java)
                    ?.initGameAudio(object : PluginCallback {
                        override fun onSuccess() {
                            setVolume(volume)
                            callback.onSuccess(roomId)
                            if (enableStream) {
                                speak(object : AudioRoomManager.AudioRoomCallback {
                                    override fun onSuccess() {

                                    }

                                    override fun onFailed(code: Int, reason: String?) {
                                    }
                                })
                            }
                            if (enablePullAllStream) {
                                listen(object : AudioRoomManager.AudioRoomCallback {
                                    override fun onSuccess() {
                                    }

                                    override fun onFailed(code: Int, reason: String?) {
                                    }
                                })
                            }
                        }

                        override fun onFailure(code: Int, reason: String?) {
                            SonaLogger.print("GameAudioPlugin 加载失败")
                        }
                    })
                registerAudioSession(true)
                enterRoomProcessing = false
            }

            override fun onFailed(code: Int, reason: String?) {
                callback.onFailed(code, reason)
                enterRoomProcessing = false
            }
        })
    }

    private fun registerPlugins() {
        addGameAudioPlugin()
    }

    private fun addGameAudioPlugin() {
        val config = AudioConfig(true, 125)
        sonaRoom?.addPlugin(GameAudioPlugin::class.java)?.config(config)
            ?.observe(object : GameAudioPluginObserver {
                override fun onReconnect() {
                    roomEventObserver?.onReceiveEvent(1002)
                }

                override fun onAudioError(p0: Int) {

                }

                override fun onDisconnect() {
                    roomEventObserver?.onReceiveEvent(1001)
                }

                override fun onSpeakerSilent(p0: Int, p1: SpeakEntity?) {}

                override fun onSpeakerSpeaking(p0: Int, p1: SpeakEntity?) {
                }

                override fun onSoundLevelInfo(soundLevelInfoList: MutableList<SoundLevelInfoEntity>?) {
                    soundLevelList = soundLevelInfoList
                }
            })
        sonaRoom?.addPlugin(AudioNoiseSuppressionPlugin::class.java)
        sonaRoom?.addPlugin(TgAudioVoiceMixPlugin::class.java)
    }

    /**
     * 调节音量
     */
    fun setVolume(volume: Int) {
        this.volume = volume
        sonaRoom?.getPlugin(GameAudioPlugin::class.java)?.setVolume(volume)
    }

    /**
     * 推流说话
     */
    fun speak(audioCallBack: AudioRoomManager.AudioRoomCallback?) {
        sonaRoom?.getPlugin(GameAudioPlugin::class.java)?.startSpeak(object : PluginCallback {
            override fun onSuccess() {
                audioCallBack?.onSuccess()
            }

            override fun onFailure(code: Int, reason: String?) {
                audioCallBack?.onFailed(code, reason)
            }
        }) ?: audioCallBack?.onFailed(ROOM_ERROR, "房间错误")
    }

    /**
     * 拉所有流
     */
    fun listen(audioCallBack: AudioRoomManager.AudioRoomCallback?) {
        sonaRoom?.getPlugin(GameAudioPlugin::class.java)?.startListen(object : PluginCallback {
            override fun onSuccess() {
                audioCallBack?.onSuccess()
            }

            override fun onFailure(p0: Int, p1: String?) {
                audioCallBack?.onFailed(p0, p1)

            }
        }) ?: audioCallBack?.onFailed(ROOM_ERROR, "房间错误")
    }

    override fun getSessionName(): String {
        return "音频房间"
    }

    override fun getSessionType(): String {
        return "audioroomV2"
    }

    override fun isRunning(): Boolean {
        return sonaRoom != null
    }

    override fun supportClose(): Boolean {
        return true
    }

    override fun close(): Boolean {
        leaveRoom(null)
        return true
    }

    /**
     * 离开房间
     */
    fun leaveRoom(audioCallBack: AudioRoomCallback?) {
        if (sonaRoom != null && !enterRoomProcessing) {
            // 已经进入房间成功，才做相关操作
            sonaRoomId = null
            sonaRoom?.observe(null)
            sonaRoom?.observeError(null)//退出房间
            sonaRoom?.getPlugin(GameAudioPlugin::class.java)?.leaveGameRoom()//退出游戏房间
            sonaRoom?.leaveRoom(object : SonaRoomCallback {
                override fun onSuccess(p0: String?) {
                    registerAudioSession(false)
                    audioCallBack?.onSuccess()
                }

                override fun onFailed(p0: Int, p1: String?) {
                    registerAudioSession(false)
                    audioCallBack?.onFailed(p0, p1)
                }
            })
        }
    }

    fun exitGame(audioCallBack: AudioRoomCallback?) {
        if (sonaRoom != null && !enterRoomProcessing) {
            // 已经进入房间成功，才做相关操作
            sonaRoomId = null
            sonaRoom?.observe(null)
            sonaRoom?.observeError(null)
            sonaRoom?.getPlugin(GameAudioPlugin::class.java)?.exitGame()//退出游戏
            sonaRoom?.leaveRoom(object : SonaRoomCallback {
                //退出房间
                override fun onSuccess(p0: String?) {
                    registerAudioSession(false)
                    audioCallBack?.onSuccess()
                }

                override fun onFailed(code: Int, reason: String?) {
                    registerAudioSession(false)
                    audioCallBack?.onFailed(code, reason)
                }
            })
            sonaRoom = null
        } else if (sonaRoom == null && !enterRoomProcessing) {
            registerAudioSession(false)
            audioCallBack?.onSuccess()
        }
    }

    /**
     * 开关麦
     */
    fun switchMic(on: Boolean, audioCallBack: AudioRoomCallback?) {
        sonaRoom?.getPlugin(GameAudioPlugin::class.java)?.switchMic(on, object : PluginCallback {
            override fun onSuccess() {
                audioCallBack?.onSuccess()
            }

            override fun onFailure(code: Int, reason: String?) {
                audioCallBack?.onFailed(code, reason)
            }
        }) ?: audioCallBack?.onFailed(ROOM_ERROR, "房间错误")
    }

    private fun registerAudioSession(register: Boolean) {
        if (register) {
            AudioSessionManager.getInstance().registerAudioSession(this)
        } else {
            AudioSessionManager.getInstance().unregisterAudioSession(this)
        }
    }

    fun sendBridgeResult(unityEvent: UnityEvent?) {
        unityEvent?.apply {
            mUnityCallback?.NativeCallback(GsonBuilder().create().toJson(unityEvent))
        }
    }

    /**
     * 对某个流静音
     */
    fun silent(streamId: String, on: Boolean, audioCallBack: AudioRoomCallback) {
        sonaRoom?.getPlugin(GameAudioPlugin::class.java)
            ?.silent(streamId, on, object : PluginCallback {
                override fun onSuccess() {
                    audioCallBack?.onSuccess()
                }

                override fun onFailure(code: Int, reason: String?) {
                    audioCallBack?.onFailed(code, reason)
                }
            }) ?: audioCallBack?.onFailed(ROOM_ERROR, "房间错误")
    }

    fun onEvent(type: String?, args: String?) {

    }

    fun currentStreamIds(): List<String>? {
        return sonaRoom?.getPlugin(GameAudioPlugin::class.java)?.currentStreamIds()
    }

    interface AudioRoomCallback {
        fun onSuccess()
        fun onFailed(code: Int, reason: String?)
    }

    interface RoomCallback {
        fun onSuccess(roomId: String?)
        fun onFailed(code: Int, reason: String?)
    }
}