package com.flyingkite.mybattery;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.flyingkite.mybattery.battery.BatteryService;
import com.flyingkite.mybattery.lockscreen.LockAdmin;
import com.flyingkite.mybattery.lockscreen.ScreenService;
import com.flyingkite.util.FilesHelper;
import com.flyingkite.util.Say;

import java.io.File;

public class MainActivity extends BaseActivity {
    private static final int REQ_ADD_ADMIN = 0xadd;
    private CheckBox openScreen;
    private CheckBox closeScreen;
    private boolean omitCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBattery();
        initScreen();
        SensorUtil.listSensors(this, Sensor.TYPE_ALL);
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
                Intent intent = new Intent(MainActivity.this, BatteryService.class);
                startService(intent);
                finish();
            }
        });
        findViewById(R.id.myFinish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Say.Log("stop Service");
                Intent intent = new Intent(MainActivity.this, BatteryService.class);
                stopService(intent);
                finish();
            }
        });
        findViewById(R.id.stopAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Say.Log("stop All Service");
                omitCheck = true;
                openScreen.setChecked(false);
                closeScreen.setChecked(false);
                stopService(new Intent(MainActivity.this, BatteryService.class));
                stopService(new Intent(MainActivity.this, ScreenService.class));
                showToast(R.string.serviceStopped);
                omitCheck = false;
            }
        });
    }

    private void initScreen() {
        openScreen = (CheckBox) findViewById(R.id.myScreenOn);
        closeScreen = (CheckBox) findViewById(R.id.myScreenOff);

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        int proxi = sm.getSensorList(Sensor.TYPE_PROXIMITY).size();

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
                if (omitCheck) return;

                makeScreenService(openScreen.isChecked(), closeScreen.isChecked());
                if (closeScreen.isChecked() && !LockAdmin.isActive(MainActivity.this)) {
                    showDialog();
                }
            }

            private void showDialog() {
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
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                closeScreen.setChecked(false);
                            }
                        }).show();
            }

            private void requestAdmin() {
                ComponentName name = new ComponentName(getApplicationContext(), LockAdmin.class);
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, name);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.addExplanation));
                startActivityForResult(intent, REQ_ADD_ADMIN);
            }
        };
        openScreen.setOnCheckedChangeListener(listener);
        closeScreen.setOnCheckedChangeListener(listener);
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
        Toast.makeText(this, sid, Toast.LENGTH_SHORT).show();
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
