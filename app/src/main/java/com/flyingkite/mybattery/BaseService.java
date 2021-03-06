package com.flyingkite.mybattery;

import android.app.Service;
import android.util.Log;

public abstract class BaseService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        logV("onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logV("onDestroy");
    }

    protected String getTagName() {
        return "Hi " + getClass().getSimpleName();
    }

    protected final void logV(String msg, Object... param) {
        logV(String.format(msg, param));
    }

    protected final void logV(String msg) {
        Log.v(getTagName(), msg);
    }

    protected final void logI(String msg, Object... param) {
        logI(String.format(msg, param));
    }

    protected final void logI(String msg) {
        Log.i(getTagName(), msg);
    }
}
