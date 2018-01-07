package com.flyingkite.mybattery.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.flyingkite.library.Say;

public class SimpleReceiver extends BroadcastReceiver {
    public interface Owner {
        void onReceive(Context context, Intent intent);
    }

    private Owner owner;

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    private void log(String format, Object... param) {
        Say.Log("RCV : " + format, param);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //log("onReceive");
        if (owner != null) {
            owner.onReceive(context, intent);
        }
    }
}
