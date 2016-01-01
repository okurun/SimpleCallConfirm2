package com.gmail.okumura.android.simplecallconfirm2;


import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity {
    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MainSettingsFragment()).commit();
    }

    /**
     * onStop
     */
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
