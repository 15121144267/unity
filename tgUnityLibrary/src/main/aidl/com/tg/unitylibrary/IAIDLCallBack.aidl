package com.tg.unitylibrary;

import com.tg.unitylibrary.UnityEvent;

interface IAIDLCallBack {
     void onEvent(in String type, in String args);
     void sendBridgeResult(in UnityEvent unityEvent);
}
