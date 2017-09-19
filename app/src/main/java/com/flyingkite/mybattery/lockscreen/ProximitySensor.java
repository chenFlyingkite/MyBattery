package com.flyingkite.mybattery.lockscreen;


import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;

import com.flyingkite.mybattery.SensorUtil;

public class ProximitySensor {
    private static final int[] SENSOR_TYPES = {Sensor.TYPE_PROXIMITY};
    private SensorManager sm;
    private WindowManager wm;
    @Deprecated
    private KeyguardManager km;
    private SensorEventListener seListener;

    public ProximitySensor(Context context, SensorEventListener listener) {
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        SensorUtil.listSensors(context, Sensor.TYPE_PROXIMITY);
        seListener = listener;
    }

    public void register() {
        for (int type : SENSOR_TYPES) {
            sm.registerListener(seListener, sm.getDefaultSensor(type), SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void unregister() {
        sm.unregisterListener(seListener);
    }
}
