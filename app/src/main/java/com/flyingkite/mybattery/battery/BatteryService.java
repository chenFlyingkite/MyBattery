package com.flyingkite.mybattery.battery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.flyingkite.mybattery.BaseService;
import com.flyingkite.mybattery.MainActivity;
import com.flyingkite.mybattery.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BatteryService extends BaseService {
    private static final String CHANNEL_ID = "com.flyingkite.mybattery.battery.BatteryService";
    private static final String CHANNEL_NAME = "BatteryService";
    private static final int NOTIF_ID = 1;
    private SimpleReceiver receiver;
    private BatteryManager batteryMgr;
    private NotificationManager notifyMgr;

    private static final SimpleDateFormat display = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.US);
    private static final SimpleDateFormat logging = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS", Locale.US);

    @Override
    public void onCreate() {
        super.onCreate();
        initSystemServices();
        createNotificationChannel();

        startForeground(NOTIF_ID, createNotification(null));

        receiver = new SimpleReceiver();
        receiver.setOwner(owner);

        IntentFilter battery = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, battery);
    }

    private void initSystemServices() {
        notifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (aboveLollipop()) {
            batteryMgr = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        }
    }

    private boolean aboveLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private boolean aboveOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    @Override
    protected String getTagName() {
        return "Hi Battery";
    }

    private int getInt(Intent intent, String name, int defaultValue) {
        if (intent == null) {
            return defaultValue;
        } else {
            return intent.getIntExtra(name, defaultValue);
        }
    }

    private int getCurrentNow() {
        if (aboveLollipop() && batteryMgr != null) {
            return batteryMgr.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        } else {
            return 0;
        }
    }

    private Notification createNotification(Intent intent) {
        Date nowD = new Date();
        String now = display.format(nowD);
        String nov = logging.format(nowD);
        int tmp = getInt(intent, BatteryManager.EXTRA_TEMPERATURE, 0);
        int vol = getInt(intent, BatteryManager.EXTRA_VOLTAGE, 0);
        //int icon = getInt(intent, BatteryManager.EXTRA_ICON_SMALL, R.mipmap.ic_launcher);

        int uA_now = getCurrentNow();

        logI("TimeTVA = ,%s,%.1f,%.1f,%.3f"
                , nov, tmp * 0.1F, vol * 1F, uA_now * 0.001F);
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.view_notification);
        rv.setTextViewText(R.id.notifHeader, getString(R.string.notificationTitle, now));
        //rv.setImageViewResource(R.id.notifIcon, icon);
        rv.setOnClickPendingIntent(R.id.notifMain, getSetIntent());
        rv.setTextViewText(R.id.notifTemperature, getString(R.string.notificationTemperature, tmp * 0.1F));
        rv.setTextViewText(R.id.notifVoltage, getString(R.string.notificationVoltage, vol * 1F));
        rv.setTextViewText(R.id.notifCurrent, getString(R.string.notificationCurrent, uA_now * 0.001F));


        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_thumb_up_white_48dp)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setContent(rv).build();
    }

    // https://developer.android.com/training/notify-user/build-notification
    private void createNotificationChannel() {
        if (aboveOreo()) {
            NotificationChannel c = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            c.enableLights(false);
            //c.setLightColor(Color.BLUE);
            c.setDescription(getString(R.string.batteryDescription));
            c.enableVibration(false);
            c.setShowBadge(false);

            notifyMgr.createNotificationChannel(c);
            logI("Channel = %s", c);
        }
    }

    private SimpleReceiver.Owner owner = (context, intent) -> {
        Notification b = createNotification(intent);

        notifyMgr.notify(NOTIF_ID, b);
    };

    private PendingIntent getSetIntent() {
        Intent it = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logV("onStartCommand(%s, %s, %s)", intent, flags, startId);
        // If we get killed, after returning from here, restart
        //return START_STICKY;
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        logV("onBind(%s)", intent);
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
