package com.flyingkite.mybattery;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseActivity extends Activity {
    private static final int REQ_PERMISSION = 1;
    private static final String[] RESULT_STATE = {"OK", "Cancel"};

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogV("result : %s", RESULT_STATE[resultCode + 1]);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogV("onNewIntent(%s)", intent);
    }

    protected String[] neededPermissions() {
        return new String[0];
    }

    protected final void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> perm = new ArrayList<>();
            String[] permissions = neededPermissions();
            perm.addAll(Arrays.asList(permissions));
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
        Log.v(getTagName(), msg);
    }

    protected final void LogI(String msg, Object... param) {
        LogI(String.format(msg, param));
    }

    protected final void LogI(String msg) {
        Log.i(getTagName(), msg);
    }

    protected final void LogE(String msg, Object... param) {
        LogE(String.format(msg, param));
    }

    protected final void LogE(String msg) {
        Log.e(getTagName(), msg);
    }

    protected final String ox(boolean b) {
        return b ? "o" : "x";
    }
}
