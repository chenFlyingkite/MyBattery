package com.flyingkite.mybattery.lockscreen;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

public class LockAdmin extends DeviceAdminReceiver {
    public static boolean isActive(Context context) {
        if (context == null) return false;
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName name = new ComponentName(context.getApplicationContext(), LockAdmin.class);
        return dpm != null && dpm.isAdminActive(name);
    }
}
