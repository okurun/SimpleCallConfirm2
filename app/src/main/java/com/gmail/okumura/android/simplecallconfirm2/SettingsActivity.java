package com.gmail.okumura.android.simplecallconfirm2;


import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MainSettingsFragment()).commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
