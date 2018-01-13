package com.flyingkite.mybattery;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SensorUtil {
    private static final String TAG = "SensorUtil";
    private SensorUtil() {}

    public static void listSensors(Context context, int type) {
        if (context == null) {
            return;
        }

        List<Sensor> list = new ArrayList<>();
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sm != null) {
            list = sm.getSensorList(type);
        }

        int n = list.size();
        log("%s sensors", n);
        for (int i = 0; i < n; i++) {
            log("#%s = %s", i, list.get(i));
        }
    }

    private static void log(String format, Object... param) {
        Log.v(TAG, String.format(format, param));
    }

}
