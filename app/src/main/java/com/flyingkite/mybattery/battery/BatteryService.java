package com.flyingkite.mybattery.battery;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.flyingkite.library.Say;
import com.flyingkite.mybattery.MainActivity;
import com.flyingkite.mybattery.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BatteryService extends Service {
    private static final int NOTIF_BATTERY = 1;
    private SimpleReceiver receiver;
    private BatteryManager btMgr;

    private static final SimpleDateFormat display = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.US);
    private static final SimpleDateFormat logging = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS", Locale.US);

    private void log(String format, Object... param) {
        Say.Log("BSV : " + format, param);
    }

    @Override
    public void onCreate() {
        log("onCreate");

        startForeground(NOTIF_BATTERY, createMyNotif(null));

        receiver = new SimpleReceiver();
        receiver.setOwner(owner);

        IntentFilter battery = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, battery);
    }

    private int getInt(Intent intent, String name, int defaultValue) {
        if (intent == null) {
            return defaultValue;
        } else {
            return intent.getIntExtra(name, defaultValue);
        }
    }

    private int getIntProp(int id) {
        int x = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (btMgr == null) {
                btMgr = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
            }
            if (btMgr != null) {
                x = btMgr.getIntProperty(id);
            }
        }
        return x;
    }

    private Notification createMyNotif(Intent intent) {
        Date nowD = new Date();
        String now = display.format(nowD);
        String nov = logging.format(nowD);
        int tmp = getInt(intent, BatteryManager.EXTRA_TEMPERATURE, 0);
        int vol = getInt(intent, BatteryManager.EXTRA_VOLTAGE, 0);
        //int icon = getInt(intent, BatteryManager.EXTRA_ICON_SMALL, R.mipmap.ic_launcher);

        int uA_now = getIntProp(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);

        //log("uA = %s", uA_now);
        log("TimeTVA = ,%s,%.1f,%.1f,%.3f"
                , nov//, getString(R.string.notificationTitle, now)
                , tmp * 0.1F//, getString(R.string.notificationTemperature, tmp * 0.1F)
                , vol * 1F//, getString(R.string.notificationVoltage, vol * 1F)
                , uA_now * 0.001F//getString(R.string.notificationCurrent, uA_now * 0.001F)
        );
        RemoteViews myRv = new RemoteViews(getPackageName(), R.layout.view_notification);
        myRv.setTextViewText(R.id.notifHeader, getString(R.string.notificationTitle, now));
        //myRv.setImageViewResource(R.id.notifIcon, icon);
        myRv.setOnClickPendingIntent(R.id.notifMain, PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        myRv.setTextViewText(R.id.notifTemperature, getString(R.string.notificationTemperature, tmp * 0.1F));
        myRv.setTextViewText(R.id.notifVoltage, getString(R.string.notificationVoltage, vol * 1F));
        myRv.setTextViewText(R.id.notifCurrent, getString(R.string.notificationCurrent, uA_now * 0.001F));

        return new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_thumb_up_white_48dp)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setContent(myRv)
                .build();
    }

    private SimpleReceiver.Owner owner = new SimpleReceiver.Owner() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Notification b = createMyNotif(intent);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.notify(NOTIF_BATTERY, b);
            }
        }
    };

    private PendingIntent getSetIntent() {
        Intent it = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("service starting");
        // If we get killed, after returning from here, restart
        //return START_STICKY;
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        log("onDestroy");
        unregisterReceiver(receiver);
    }
}
