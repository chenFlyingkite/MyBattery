package com.flyingkite.mybattery;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogV("onCreate %s", savedInstanceState);
        requestPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogV("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogV("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogV("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogV("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogV("onDestroy");
    }

    private static final int REQ_PERMISSION = 1;

    protected String[] neededPermissions() {
        return new String[0];
    }

    protected final void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> perm = new ArrayList<>();
            String[] permissions = neededPermissions();
            perm.addAll(Arrays.asList(permissions));
//            Collections.addAll(perm
//                    , Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    , Manifest.permission.RECORD_AUDIO);
            for (int i = perm.size() - 1; i >= 0; i--) {
                if (checkSelfPermission(perm.get(i)) == PackageManager.PERMISSION_GRANTED) {
                    perm.remove(i);
                }
            }
            if (perm.size() > 0) {
                requestPermissions(perm.toArray(new String[perm.size()]), REQ_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSION:
                LogV("req permissions = " + Arrays.toString(permissions));
                LogV("req result = " + Arrays.toString(grantResults));
                break;
        }
    }

    protected final String getTagName() {
        return "Hi " + getClass().getSimpleName();
    }

    protected final void LogV(String msg, Object... param) {
        LogV(String.format(msg, param));
    }

    protected final void LogV(String msg) {
        Log.w(getTagName(), msg);
    }

    protected final void LogE(String msg, Object... param) {
        LogE(String.format(msg, param));
    }

    protected final void LogE(String msg) {
        Log.e(getTagName(), msg);
    }
}
