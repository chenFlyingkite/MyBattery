package com.flyingkite.mybattery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingkite.library.Say;
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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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
        findViewById(R.id.myBattery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Say.Log("start Service");
                makeBatteryService(true);
            }
        });
        findViewById(R.id.myFinish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Say.Log("stop Service");
                makeBatteryService(false);
            }
        });
        findViewById(R.id.stopAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Say.Log("stop All Service");
                closeScreen.setChecked(false);
                openScreen.setChecked(false);
                makeBatteryService(false);
                finish();
            }
        });

        findViewById(R.id.startAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Say.Log("start All Service");
                closeScreen.setChecked(true);
                openScreen.setChecked(true);
                makeBatteryService(true);
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
            openScreen.setVisibility(View.GONE);
            closeScreen.setVisibility(View.GONE);
            return;
        }

        openScreen.setChecked(hasLogCache(".opening"));
        closeScreen.setChecked(hasLogCache(".closing"));

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                makeScreenService();
            }
        };
        openScreen.setOnCheckedChangeListener(listener);
        closeScreen.setOnCheckedChangeListener(listener);
    }

    private void initAudio() {
        ringer = findViewById(R.id.audioRingerIcon);
        musicMode = findViewById(R.id.audioMusic);
        ringerMode = findViewById(R.id.audioRinger);
        final AudioManager am = audioManager;

        showAudioState();
        findViewById(R.id.audioManagerChange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (am == null) return;

                int mode = am.getRingerMode();
                int flag = AudioManager.FLAG_SHOW_UI
                        | AudioManager.FLAG_PLAY_SOUND
                        | AudioManager.FLAG_VIBRATE;

                if (mode == AudioManager.RINGER_MODE_NORMAL) {
                    am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    // -100 = AudioManager.ADJUST_MUTE
                    am.adjustStreamVolume(AM_MUSIC, -100, flag);
                } else {
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    // 100 = AudioManager.ADJUST_UNMUTE
                    am.adjustStreamVolume(AM_MUSIC, 100, flag);
                }
                showAudioState();
            }
        });
    }

    private void showAudioState() {
        AudioManager am = audioManager;

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
        Say.Log("mode %s -> %s, music = %s / %s", mode, rings[mode], vol, max);

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
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeScreen.setChecked(false);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestAdmin();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        closeScreen.setChecked(false);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        askingAdmin = false;
                    }
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
        makeScreen();
        checkAdmin();
    }

    private void makeScreen() {
        makeScreenService(openScreen.isChecked(), closeScreen.isChecked());
    }

    private void makeScreenService(boolean open, boolean close) {
        Intent intent = new Intent(MainActivity.this, ScreenService.class);
        if (open || close) {
            Say.Log("start Service : ScreenService : %s %s", Say.ox(open), Say.ox(close));
            intent.putExtra(ScreenService.EXTRA_OPEN_SCREEN, open);
            intent.putExtra(ScreenService.EXTRA_CLOSE_SCREEN, close);
            startService(intent);
            showToast(R.string.serviceStarted);
        } else {
            Say.Log("stop Service : ScreenService");
            stopService(intent);
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
        String[] state = {"OK", "Cancel"};
        Say.Log("result : %s", state[resultCode + 1]);
        if (requestCode == REQ_ADD_ADMIN) {
            if (resultCode == RESULT_OK) {
                Say.Log("Admin add");
            }
            closeScreen.setChecked(resultCode == RESULT_OK);
        }
    }
}
