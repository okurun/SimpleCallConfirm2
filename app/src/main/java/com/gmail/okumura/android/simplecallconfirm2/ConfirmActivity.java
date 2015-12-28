package com.gmail.okumura.android.simplecallconfirm2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.widget.Toast;

public class ConfirmActivity extends Activity {
    private static final String SCHEME_TEL = "tel:";
    private static final int REQUEST_CODE_REQUEST_CALL_PERMISSION = 1;

    private String mNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mNumber = getIntent().getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        if (MainSettingsFragment.isFingerprintConfirm(this)) {
            showFingerprintConfirmDialog();
        } else {
            showConfirmDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNumber = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_REQUEST_CALL_PERMISSION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    MainSettingsFragment.setEnabled(this, false);
                    Toast.makeText(this, R.string.disable_confirm_message, Toast.LENGTH_LONG).show();
                    Toast.makeText(this, R.string.please_recall, Toast.LENGTH_LONG).show();
                    return;
                }
                call();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showFingerprintConfirmDialog() {
        if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
            FingerprintManager fingerprintManager =
                    (FingerprintManager)getSystemService(Activity.FINGERPRINT_SERVICE);
            if (fingerprintManager.hasEnrolledFingerprints()) {
                fingerprintManager.authenticate(null, null, 0, new FingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                    }

                    @Override
                    public void onAuthenticationFailed() {
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        call();
                    }
                }, new Handler());
            } else {
                MainSettingsFragment.setFingerprintConfirm(this, false);
                Toast.makeText(this, R.string.not_has_enrolled_fingerprints, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showConfirmDialog() {
        int style = MainSettingsFragment.getTheme(this);
        (new AlertDialog.Builder(new ContextThemeWrapper(this, style)))
                .setTitle(mNumber)
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher)
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

    private void call() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE_REQUEST_CALL_PERMISSION);
                return;
            }
        }

        if (null == mNumber) {
            return;
        }

        OutgoingCallsReceiver.setIgnoreConfirm(true);

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(SCHEME_TEL + mNumber));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
