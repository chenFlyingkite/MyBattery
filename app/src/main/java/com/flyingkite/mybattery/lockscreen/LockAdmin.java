package com.flyingkite.mybattery.lockscreen;

import android.app.AlertDialog;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.flyingkite.mybattery.R;

public class LockAdmin extends DeviceAdminReceiver {
    public static boolean isActive(Context context) {
        if (context == null) return false;
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName name = new ComponentName(context.getApplicationContext(), LockAdmin.class);
        return dpm != null && dpm.isAdminActive(name);
    }

    public static boolean checkActive(Context context) {
        if (isActive(context)) {
            return true;
        } else {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.needDeviceAdmin)
                    .setMessage(R.string.findInDeviceAdmin)
                    .show();
            return false;
        }
    }

    public static Intent getAddDeviceAdminIntent(Context context) {
        ComponentName name = new ComponentName(context.getApplicationContext(), LockAdmin.class);
        Intent it = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        it.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, name);
        it.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.addExplanation));
        return it;
    }
}
