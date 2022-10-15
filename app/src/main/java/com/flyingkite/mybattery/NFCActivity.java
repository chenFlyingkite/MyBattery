package com.flyingkite.mybattery;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;

import java.util.Arrays;

public class NFCActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
    }

    @Override
    protected void onResume() {
        super.onResume();
        peek(getIntent());
    }

    private void peek(Intent intent) {
        if (intent == null) return;

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //NdefMessage[] ndef = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            LogE("tag, id = %s, tech = %s", Arrays.toString(tag.getId()), Arrays.toString(tag.getTechList()));
        }
    }
}
