package com.flyingkite.mybattery.lockscreen;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.flyingkite.mybattery.SensorUtil;

public class ProximitySensor {
    private static final int[] SENSOR_TYPES = {Sensor.TYPE_PROXIMITY};
    private SensorManager sm;
    private SensorEventListener seListener;

    public ProximitySensor(Context context, SensorEventListener listener) {
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        SensorUtil.listSensors(context, SENSOR_TYPES[0]);
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
