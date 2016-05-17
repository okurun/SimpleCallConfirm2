package com.gmail.okumura.android.simplecallconfirm2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.gmail.okumura.android.simplecallconfirm2.settings.SettingsManager;

import java.security.KeyStore;
import java.security.Signature;

/**
 * Created by naoki on 16/01/16.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerprintDialogFragment extends DialogFragment {
    private static final String TAG = FingerprintDialogFragment.class.getSimpleName();

    private CharSequence mTitle = null;
    private CharSequence mMessage = null;
    private FingerprintManager mFingerprintManager = null;
    private FingerprintManager.AuthenticationCallback mCallback = null;
    private CancellationSignal mCancellationSignal = null;
    private Signature mSignature = null;
    private KeyStore mKeyStore = null;
    private boolean mEnableFinish = false;

    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    public void setMessage(CharSequence message) {
        mMessage = message;
    }

    public void setCallback(FingerprintManager.AuthenticationCallback callback) {
        mCallback = callback;
    }

    public void setEnableFinish(boolean enable) {
        mEnableFinish = enable;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog");
        int style = SettingsManager.getIntTheme(getActivity().getApplicationContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), style));
        builder.setIcon(R.drawable.ic_fp_40px);
        if (null != mTitle) {
            builder.setTitle(mTitle);
        }
        if (null != mMessage) {
            builder.setMessage(mMessage);
        }

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        Context context = getActivity().getApplicationContext();
        mFingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);

        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);
        } catch (Exception e) {
            Log.e(TAG, "Initialize KeyStore error", e);
            return;
        }

        if (context.checkSelfPermission(Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            // 権限がないので何もしない
            return;
        }

        mCancellationSignal = new CancellationSignal();
        mFingerprintManager.authenticate(
                new FingerprintManager.CryptoObject(mSignature), mCancellationSignal, 0, mCallback, null);

        Log.i(TAG, "end onResume");
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mEnableFinish) {
            getActivity().finish();
        }
    }
}
