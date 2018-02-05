package com.flyingkite.mybattery.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SimpleReceiver extends BroadcastReceiver {
    public interface Owner {
        void onReceive(Context context, Intent intent);
    }

    private Owner owner;

    public void setOwner(Owner owner) {
        this.owner = owner;
    }


    protected final void logI(String msg, Object... param) {
        logI(String.format(msg, param));
    }

    protected final void logI(String msg) {
        Log.i("Hi SRV", msg);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //log("onReceive");
        if (owner != null) {
            owner.onReceive(context, intent);
        }
    }
}
