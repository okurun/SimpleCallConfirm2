package com.gmail.okumura.android.simplecallconfirm2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_REQUEST_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (MainSettingsFragment.hasPermissions(getApplicationContext())) {
                String[] permissions = new String[] {
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.CALL_PHONE,
                };
                requestPermissions(permissions, REQUEST_CODE_REQUEST_PERMISSIONS);
                return;
            }
        }

        startNextActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_REQUEST_PERMISSIONS:
                for (int i = 0; i < 2; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        MainSettingsFragment.setEnabled(this, false);
                        Toast.makeText(this, R.string.disable_confirm_message, Toast.LENGTH_LONG).show();
                    }
                }
                startNextActivity();
                break;
        }
    }

    private void startNextActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }
}
