package com.flyingkite.mybattery;

import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingkite.mybattery.battery.BatteryService;
import com.flyingkite.mybattery.lockscreen.LockAdmin;
import com.flyingkite.mybattery.lockscreen.ScreenService;
import com.flyingkite.util.FilesHelper;

import java.io.File;

public class MainActivity extends BaseActivity {
    // static fields
    private static final int REQ_ADD_ADMIN = 0xadd;
    private static final int AM_MUSIC = AudioManager.STREAM_MUSIC;

    // Views
    private CheckBox openScreen;
    private CheckBox closeScreen;
    private TextView musicMode;
    private TextView ringerMode;
    private ImageView ringer;

    // Components
    private AudioManager audioManager;
    private SensorManager sensorManager;
    private boolean askingAdmin;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initBattery();
        initScreen();
        initAudio();
        SensorUtil.listSensors(this, Sensor.TYPE_ALL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LockAdmin.isActive(MainActivity.this)) {
            closeScreen.setChecked(false);
        }
        showAudioState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuDeviceAdmin:
                startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                return true;
        }
        return false;
    }

    private void initBattery() {
        findViewById(R.id.myBattery).setOnClickListener(v -> {
                LogI("start Service");
                makeBatteryService(true);
            }
        );
        findViewById(R.id.myFinish).setOnClickListener(v -> {
            LogI("stop Service");
            makeBatteryService(false);
        });
        findViewById(R.id.stopAll).setOnClickListener(v -> {
            LogI("stop All Service");
            closeScreen.setChecked(false);
            openScreen.setChecked(false);
            makeBatteryService(false);
            makeScreenService();
        });
        findViewById(R.id.startAll).setOnClickListener(v -> {
            LogI("start All Service");
            closeScreen.setChecked(true);
            openScreen.setChecked(true);
            makeBatteryService(true);
            makeScreenService();
            if (LockAdmin.isActive(MainActivity.this)) {
                finish();
            }
        });
    }

    private void initScreen() {
        openScreen = findViewById(R.id.myScreenOn);
        closeScreen = findViewById(R.id.myScreenOff);
        final SensorManager sm = sensorManager;
        int proxi = 0;
        if (sm != null) {
            proxi = sm.getSensorList(Sensor.TYPE_PROXIMITY).size();
        }

        if (proxi == 0) {
            findViewById(R.id.mySensorNotFound).setVisibility(View.VISIBLE);
            findViewById(R.id.myScreenPanel).setVisibility(View.GONE);
            return;
        }

        openScreen.setChecked(hasLogCache(".opening"));
        closeScreen.setChecked(hasLogCache(".closing"));

        View.OnClickListener onClick = (v) -> {
            makeScreenService();
        };

        openScreen.setOnClickListener(onClick);
        closeScreen.setOnClickListener(onClick);
        findViewById(R.id.screenApply).setOnClickListener(onClick);
    }

    private void initAudio() {
        ringer = findViewById(R.id.audioRingerIcon);
        musicMode = findViewById(R.id.audioMusic);
        ringerMode = findViewById(R.id.audioRinger);
        findViewById(R.id.musicAdd).setOnClickListener((v) -> {
            adjustStreamVolume(1);
            showAudioState();
        });
        findViewById(R.id.musicMinus).setOnClickListener((v) -> {
            adjustStreamVolume(-1);
            showAudioState();
        });

        final AudioManager am = audioManager;

        showAudioState();
        findViewById(R.id.audioManagerChange).setOnClickListener(v -> {
            if (am == null) return;

            final int mode = am.getRingerMode();

            int nextMode = mode;
            // -100 = AudioManager.ADJUST_MUTE
            // 0 = AudioManager.ADJUST_SAME
            // 100 = AudioManager.ADJUST_UNMUTE
            int direction = 0;

            if (mode == AudioManager.RINGER_MODE_NORMAL) {
                nextMode = AudioManager.RINGER_MODE_VIBRATE;
                direction = -100;
            } else {
                nextMode = AudioManager.RINGER_MODE_NORMAL;
                direction = 100;
                if (mode == AudioManager.RINGER_MODE_SILENT) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // From N onward, ringer mode adjustments that would toggle Do Not Disturb are not allowed
                        // unless the app has been granted Do Not Disturb Access.
                        // See AudioManager#setRingerMode()
                        nextMode = mode;
                        direction = 0;
                    }
                }
            }

            am.setRingerMode(nextMode);
            adjustStreamVolume(direction);

            showAudioState();
        });
    }

    private void adjustStreamVolume(int direction) {
        final AudioManager am = audioManager;
        if (am == null) return;
        // -100 = AudioManager.ADJUST_MUTE
        // 0 = AudioManager.ADJUST_SAME
        // 100 = AudioManager.ADJUST_UNMUTE
        final int flag = AudioManager.FLAG_SHOW_UI
                //| AudioManager.FLAG_ALLOW_RINGER_MODES // It lost mute volume
                | AudioManager.FLAG_PLAY_SOUND
                | AudioManager.FLAG_VIBRATE;

        am.adjustStreamVolume(AM_MUSIC, direction, flag);
    }

    private void showAudioState() {
        final AudioManager am = audioManager;

        if (am == null) {
            musicMode.setText(R.string.na);
            ringerMode.setText(R.string.na);
            return;
        }

        final int[] iconIds = {R.drawable.ic_do_not_disturb_on_black_48dp
                , R.drawable.ic_vibration_black_48dp
                , R.drawable.ic_notifications_black_48dp
        };
        final String[] rings = {getString(R.string.silent), getString(R.string.vibrate), getString(R.string.normal)};
        int mode = am.getRingerMode();
        int vol = am.getStreamVolume(AM_MUSIC);
        int max = am.getStreamMaxVolume(AM_MUSIC);
        LogI("Audio mode %s -> %s, music = %s / %s", mode, rings[mode], vol, max);

        ringer.setImageResource(iconIds[mode]);
        musicMode.setText(getString(R.string.ratio, vol, max));
        ringerMode.setText(rings[mode]);
    }

    private void showDialog() {
        if (askingAdmin) return;

        askingAdmin = true;
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.needDeviceAdmin)
                .setMessage(R.string.findInDeviceAdmin)
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    closeScreen.setChecked(false);
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    requestAdmin();
                })
                .setOnCancelListener(dialog -> {
                    closeScreen.setChecked(false);
                })
                .setOnDismissListener(dialog -> {
                    askingAdmin = false;
                })
                .show();
    }

    private void requestAdmin() {
        Intent intent = LockAdmin.getAddDeviceAdminIntent(MainActivity.this);
        startActivityForResult(intent, REQ_ADD_ADMIN);
    }

    private void makeBatteryService(boolean start) {
        Intent it = new Intent(MainActivity.this, BatteryService.class);
        if (start) {
            startService(it);
        } else {
            stopService(it);
        }
    }

    private void checkAdmin() {
        if (closeScreen.isChecked() && !LockAdmin.isActive(MainActivity.this)) {
            showDialog();
        }
    }

    private void makeScreenService() {
        parseParam();
        makeScreen();
        checkAdmin();
    }

    private int inertia;
    private int time;

    private void parseParam() {
        time = parse(findViewById(R.id.paramTime));
        inertia = parse(findViewById(R.id.paramInertia));
    }

    private int parse(TextView t) {
        return Integer.parseInt(t.getText().toString());
    }

    private void makeScreen() {
        makeScreenService(openScreen.isChecked(), closeScreen.isChecked());
    }

    private void makeScreenService(boolean open, boolean close) {
        Intent it = new Intent(MainActivity.this, ScreenService.class);
        if (open || close) {
            LogI("start Service : ScreenService : %s %s", ox(open), ox(close));
            it.putExtra(ScreenService.EXTRA_OPEN_SCREEN, open);
            it.putExtra(ScreenService.EXTRA_CLOSE_SCREEN, close);
            it.putExtra(ScreenService.EXTRA_PARAM_TIME, time);
            it.putExtra(ScreenService.EXTRA_PARAM_INERTIA, inertia);
            startService(it);
            showToast(R.string.serviceStarted);
        } else {
            LogI("stop Service : ScreenService");
            stopService(it);
            showToast(R.string.serviceStopped);
        }
        // Remember as file
        logAsCacheFile(".opening", open);
        logAsCacheFile(".closing", close);
    }

    private void logAsCacheFile(String name, boolean create) {
        // Remember as file
        File file = new File(getExternalCacheDir(), name);
        if (create) {
            FilesHelper.createNewFile(file);
        } else {
            FilesHelper.fullDelete(file);
        }
    }

    private boolean hasLogCache(String name) {
        return new File(getExternalCacheDir(), name).exists();
    }

    private void showToast(@StringRes int sid) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(MainActivity.this, sid, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ADD_ADMIN) {
            if (resultCode == RESULT_OK) {
                LogV("Admin add");
            }
            closeScreen.setChecked(resultCode == RESULT_OK);
        }
    }
}
