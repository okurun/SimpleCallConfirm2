package com.gmail.okumura.android.simplecallconfirm2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.widget.Toast;

import com.gmail.okumura.android.simplecallconfirm2.settings.MainSettingsFragment;
import com.gmail.okumura.android.simplecallconfirm2.settings.SettingsManager;

public class ConfirmActivity extends Activity {
    private static final int REQUEST_CODE_REQUEST_CALL_PERMISSION = 1;

    private String mNumber = null;

    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mNumber = getIntent().getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        if (SettingsManager.isFingerprintConfirm(this)) {
            showFingerprintConfirmDialog();
        } else {
            showConfirmDialog();
        }
    }

    /**
     * onDestroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNumber = null;
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
            case REQUEST_CODE_REQUEST_CALL_PERMISSION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    SettingsManager.setCallConfirmEnabled(this, false);
                    Toast.makeText(this, R.string.disable_confirm_message, Toast.LENGTH_LONG).show();
                    Toast.makeText(this, R.string.please_recall, Toast.LENGTH_LONG).show();
                    return;
                }
                call();
                break;
        }
    }

    /**
     * 指紋認証ダイアログを表示します
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void showFingerprintConfirmDialog() {
        if (MainSettingsFragment.hasFingerprintPermissions(this)) {
            if (MainSettingsFragment.hasEnrolledFingerprints(this)) {
                // TODO ここが動作しない
//                fingerprintManager.authenticate(null, null, 0, new FingerprintManager.AuthenticationCallback() {
//                    @Override
//                    public void onAuthenticationError(int errorCode, CharSequence errString) {
//                    }
//
//                    @Override
//                    public void onAuthenticationFailed() {
//                    }
//
//                    @Override
//                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
//                        call();
//                    }
//                }, new Handler());

                return;
            }
        }

        // 指紋認証が不可能な状態なので指紋認証を無効にします
        SettingsManager.setFingerprintConfirm(this, false);
        Toast.makeText(this, R.string.not_has_enrolled_fingerprints, Toast.LENGTH_LONG).show();
    }

    /**
     * 通常の発信確認ダイアログを表示する
     */
    private void showConfirmDialog() {
        int style = SettingsManager.getIntTheme(this);
        (new AlertDialog.Builder(new ContextThemeWrapper(this, style)))
                .setTitle(mNumber)
                .setCancelable(true)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        call();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * 電話を発信します
     */
    private void call() {
        // 電話の発信権限があるか確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS)
                        != PackageManager.PERMISSION_GRANTED) {
            // 電話の発信権限をリクエストする
            MainSettingsFragment.requestCallConfirmPermissions(this, REQUEST_CODE_REQUEST_CALL_PERMISSION);
            return;
        }

        // 発信先がなければ何もしない
        if (null == mNumber) {
            return;
        }

        OutgoingCallsReceiver.setIgnoreConfirm(true);

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(OutgoingCallsReceiver.SCHEME_TEL + mNumber));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
