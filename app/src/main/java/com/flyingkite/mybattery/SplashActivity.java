package com.flyingkite.mybattery;


import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0 && !isTaskRoot()) {
            LogV("Other activities of App exists. Finish splash to show existing activities.");
            finish();
            return;
        }
        //--- Put Preload tasks here

        //--- End of tasks, we will goto Main
        starts();
    }

    private void starts() {
        if (isTaskRoot()) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            LogV("Splash is the first. Let's goto Main.");
        } else {
            LogV("Splash is not first. Finish splash.");
            finish();
        }
    }
}
