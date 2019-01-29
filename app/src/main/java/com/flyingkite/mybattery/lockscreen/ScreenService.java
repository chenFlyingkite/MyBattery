package com.flyingkite.mybattery.lockscreen;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import com.flyingkite.mybattery.BaseService;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenService extends BaseService {
    public static final String EXTRA_PARAM_TIME = "EXTRA_PARAM_TIME";
    public static final String EXTRA_OPEN_SCREEN = "EXTRA_OPEN_SCREEN";
    public static final String EXTRA_CLOSE_SCREEN = "EXTRA_CLOSE_SCREEN";
    public static final String EXTRA_PARAM_INERTIA = "EXTRA_PARAM_INERTIA";
    private ProximitySensor proximity;
    private PowerManager powerMgr;
    private KeyguardManager keyguardMgr;
    private DevicePolicyManager policyMgr;

    private boolean enableOpen;
    private boolean enableClose;
    // We will perform lock/unlock when proximity sensor events
    // Receive >= [inertia] events within [time] millisecond
    // Parameters
    private static final int _inertia = 6;
    private static final int _time = 1000;
    private static int inertia;
    private static long time;
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicBoolean working = new AtomicBoolean(false);
    private final ResetHandler reset = new ResetHandler();

    @Override
    public void onCreate() {
        super.onCreate();
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
        proximity.register();
        if (intent != null) {
            enableOpen = intent.getBooleanExtra(EXTRA_OPEN_SCREEN, false);
            enableClose = intent.getBooleanExtra(EXTRA_CLOSE_SCREEN, false);
            int in = intent.getIntExtra(EXTRA_PARAM_INERTIA, _inertia);
            int ti = intent.getIntExtra(EXTRA_PARAM_TIME, _time);
            inertia = in > 0 ? in : _inertia;
            time = ti > 0 ? ti : _time;
        }
        logV("onStartCommand, open = %s, close = %s", enableOpen, enableClose);
        logV("inertia = %s, time = %s", inertia, time);
        // If we get killed, after returning from here, NO NEED restart
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        proximity.unregister();
    }

    @Override
    protected String getTagName() {
        return "Hi Screen";
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
            logV("Sensor event = %s, %s, %s, %s"
                    , event.accuracy
                    , Arrays.toString(event.values)
                    , new Date(event.timestamp).toGMTString()
                    , event.sensor
            );
            int n = count.incrementAndGet();
            boolean locked = keyguardMgr.inKeyguardRestrictedInputMode();
            boolean isOn = isScreenOn();
            logV("n = %s, locked = %s, screen on = %s", n, locked, isOn);
            if (n >= inertia && !working.get()) {
                if (isOn) {
                    if (enableClose) {
                        logI("lockNow");
                        working.set(true);
                        if (LockAdmin.isActive(ScreenService.this)) {
                            policyMgr.lockNow();
                        }
                        resendReset();
                    }
                } else {
                    if (enableOpen) {
                        logI("wake");
                        working.set(true);
                        wake();
                        resendReset();
                    }
                }
            }
            if (n > 0 && !working.get()) {
                resendResetDelayed(time);
            }
        }

        private void resendReset() {
            reset.removeCallbacks(runReset);
            reset.post(runReset);
        }

        private void resendResetDelayed(long millis) {
            reset.removeCallbacks(runReset);
            reset.postDelayed(runReset, millis);
        }

        private Runnable runReset = () -> {
            logV("reset as 0");
            count.set(0);
            working.set(false);
        };

        private void wake() {
            // X_X Failed
//            Intent it = new Intent(BatteryService.this, WakeUpActivity.class);
//            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(it);

            // X_X
            //km.newKeyguardLock("tag").disableKeyguard();

            int flag = PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
            PowerManager.WakeLock wl = powerMgr.newWakeLock(flag, "screen:lock");
            wl.acquire(100);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            logV("onAccuracyChanged(), acc = %s, sensor = %s", accuracy, sensor.getName());
        }
    };

    private static class ResetHandler extends Handler {

    }
}
