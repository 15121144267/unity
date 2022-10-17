package com.tg.unitylibrary;

import com.tg.unitylibrary.UnityEvent;
import com.tg.unitylibrary.IAIDLCallBack;

interface IUnityPluginContext {
    void enterRoom(in String roomId, in String product, in boolean enablePullAllStream,
                       in boolean enableStream, in boolean enableAudioSonic,in UnityEvent unityEvent);
    void speak(in int status, in String roomId,in UnityEvent unityEvent);
    void listen(in int status, in String roomId,in UnityEvent unityEvent);
    void listenStream(in List<String> uids, in String roomId,in UnityEvent unityEvent);
    void switchMic(in int status, in String roomId,in UnityEvent unityEvent);
    void leaveAudioRoom(in String roomId,in UnityEvent unityEvent);
    void exitGame(in String roomId,in UnityEvent unityEvent);
    void setAIDLCallBack(IAIDLCallBack callback);
    void muteUid(in String uid, in int status, in String roomId, in UnityEvent unityEvent);
    void muteUids(in List<String> uids, in int status, in String roomId, in UnityEvent unityEvent);
    void goLogin(in String type, in UnityEvent unityEvent);
    void tracker(String data, boolean upload);
    void soraka(String scene, String event,String reason,String content);
    void closeDialog();
}