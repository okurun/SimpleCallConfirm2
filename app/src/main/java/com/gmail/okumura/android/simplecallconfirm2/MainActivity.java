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

    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 発信確認に必要なパーミッションの確認
        if (!MainSettingsFragment.hasCallConfirmPermissions(getApplicationContext())) {
            // 発信確認に必要なパーミッションをリクエスト
            MainSettingsFragment.requestCallConfirmPermissions(this, REQUEST_CODE_REQUEST_PERMISSIONS);
            return;
        }

        startNextActivity();
    }

    /**
     * onRequestPermissionsResult
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_REQUEST_PERMISSIONS:
                for (int i = 0; i < 2; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        // パーミッションがないのでSimpleCallConfirmを無効にする
                        MainSettingsFragment.setCallConfirmEnabled(this, false);
                        Toast.makeText(this, R.string.disable_confirm_message, Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                startNextActivity();
                break;
        }
    }

    /**
     * 次のアクティビティを起動する
     */
    private void startNextActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }
}
