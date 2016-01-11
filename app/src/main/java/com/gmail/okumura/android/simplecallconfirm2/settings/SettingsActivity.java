package com.gmail.okumura.android.simplecallconfirm2.settings;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;


public class SettingsActivity extends PreferenceActivity {
    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(SettingsManager.getIntTheme(this));
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MainSettingsFragment()).commit();
    }
}
