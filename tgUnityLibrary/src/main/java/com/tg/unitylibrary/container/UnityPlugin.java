package com.tg.unitylibrary.container;

import android.app.Activity;
import android.content.Intent;

import com.tg.unitylibrary.IntergrationInterface;
import com.tg.unitylibrary.UnityEvent;

public interface UnityPlugin {
    void onPrepare(UnityEventFilter cocosEventFilter);

    void onInitialize();

    void handleEvent(Activity context, UnityEvent cocosEvent,IntergrationInterface callback);

    void onResult(int requestCode, int resultCode, Intent data);
}
