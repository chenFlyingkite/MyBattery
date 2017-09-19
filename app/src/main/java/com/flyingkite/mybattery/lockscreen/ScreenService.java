package com.flyingkite.mybattery.lockscreen;

import android.app.KeyguardManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;

import com.flyingkite.util.Say;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenService extends Service {
    public static final String EXTRA_OPEN_SCREEN = "EXTRA_OPEN_SCREEN";
    public static final String EXTRA_CLOSE_SCREEN = "EXTRA_CLOSE_SCREEN";
    private ProximitySensor proximity;
    private PowerManager powerMgr;
    private KeyguardManager keyguardMgr;
    private DevicePolicyManager policyMgr;

    private boolean enableOpen;
    private boolean enableClose;
    // We will perform lock/unlock when proximity sensor events
    // Receive >= [inertia] events within [time] millisecond
    private static final int inertia = 6;
    private static final long time = 2000;
    private AtomicInteger count = new AtomicInteger(0);
    private AtomicBoolean working = new AtomicBoolean(false);
    private ResetHandler reset = new ResetHandler();

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate");
        proximity = new ProximitySensor(this, seListener);
        powerMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        keyguardMgr = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        policyMgr = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");
        proximity.register();
        if (intent != null) {
            enableOpen = intent.getBooleanExtra(EXTRA_OPEN_SCREEN, false);
            enableClose = intent.getBooleanExtra(EXTRA_CLOSE_SCREEN, false);
            log(" OP = %s, CL = %s", enableOpen, enableClose);
        }
        // If we get killed, after returning from here, NO NEED restart
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
        proximity.unregister();
    }

    private void log(String format, Object... param) {
        Say.Log("SCR : " + format, param);
    }

    private boolean isScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerMgr.isInteractive();
        } else {
            return powerMgr.isScreenOn();
        }
    }

    private SensorEventListener seListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Say.Log("Sensor event = %s, %s, %s, %s"
                    , event.accuracy
                    , Arrays.toString(event.values)
                    , new Date(event.timestamp).toGMTString()
                    , event.sensor
            );
            int n = count.incrementAndGet();
            boolean locked = keyguardMgr.inKeyguardRestrictedInputMode();
            boolean isOn = isScreenOn();
            Say.Log("n = %s, locked = %s, screen on = %s", n, locked, isOn);
            if (n >= inertia && !working.get()) {
                if (isOn) {
                    if (enableClose) {
                        Say.Log("lockNow");
                        working.set(true);
                        policyMgr.lockNow();
                        resendReset();
                    }
                } else {
                    if (enableOpen) {
                        Say.Log("wake");
                        working.set(true);
                        wake();
                        resendReset();
                    }
                }
            }
            if (n > 0 && !working.get()) {
                if (!reset.hasMessages(RESET)) {
                    reset.sendEmptyMessageDelayed(RESET, time);
                }
            }
        }

        private void resendReset() {
            reset.removeMessages(RESET);
            reset.sendEmptyMessage(RESET);
        }

        private void wake() {
            // X_X Failed
//            Intent it = new Intent(BatteryService.this, WakeUpActivity.class);
//            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(it);

            // X_X
            //km.newKeyguardLock("tag").disableKeyguard();

            int flag = PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
            PowerManager.WakeLock wl = powerMgr.newWakeLock(flag, "tag");
            wl.acquire(100);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Say.Log("Acc = %s, sensor = %s", accuracy, sensor.getName());
        }
    };

    private static final int RESET = 0;
    private class ResetHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESET:
                    Say.Log("reset as 0");
                    count.set(0);
                    working.set(false);
                    break;
            }
        }
    }
}
