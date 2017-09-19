package com.flyingkite.mybattery;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

public class WakeUpActivity extends BaseActivity {
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        int flag = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().addFlags(flag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "I'm alive", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //finish();
    }
}
