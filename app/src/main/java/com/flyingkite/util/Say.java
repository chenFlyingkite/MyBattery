package com.flyingkite.util;

import android.util.Log;

public class Say {
    private static final String TAG = "Hi";

    public static void Log(String msg) {
        Log.e(TAG, msg);
    }

    public static void Log(String format, Object... params) {
        Log(String.format(format, params));
    }

    public static String ox(boolean b) {
        return b ? "o" : "x";
    }
}
