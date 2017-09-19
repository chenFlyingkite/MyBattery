package com.flyingkite.mybattery;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.flyingkite.util.Say;

import java.util.ArrayList;
import java.util.List;

public class SensorUtil {
    private SensorUtil() {}

    public static void listSensors(Context context, int type) {
        if (context == null) {
            Say.Log("Null context");
            return;
        }

        List<Sensor> list = new ArrayList<>();
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sm != null) {
            list = sm.getSensorList(type);
        }

        int n = list.size();
        Say.Log("%s items", n);
        for (int i = 0; i < n; i++) {
            Say.Log("#%s = %s", i, list.get(i));
        }
    }

}
