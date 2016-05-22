package com.gmail.okumura.android.simplecallconfirm2.settings;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.gmail.okumura.android.simplecallconfirm2.FingerprintDialogFragment;
import com.gmail.okumura.android.simplecallconfirm2.R;
import com.gmail.okumura.android.simplecallconfirm2.widget.MainWidgetUpdateReceiver;

import java.util.Iterator;

/**
 * Created by naoki on 15/12/27.
 */
public class MainSettingsFragment extends PreferenceFragment implements RefreshDisplayInterface {
    private static final String PREF_ADD_BLUETOOTH_DEVICE = "add_bluetooth_device";
    private static final String PREF_REMOVE_BLUETOOTH_DEVICE = "remove_bluetooth_device";

    private static final int REQUEST_CODE_REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_CODE_REQUEST_FINGERPRINT_PERMISSIONS = 2;
    private static final int REQUEST_CODE_REQUEST_BLUETOOTH_PERMISSIONS = 3;

    private static final String DIALOG_ADD_BLUETOOTH_DEVICE_LIST = "add_bluetooth_device_list";
    private static final String DIALOG_REMOVE_BLUETOOTH_DEVICE_LIST = "remove_bluetooth_device_list";

    /**
     * テーマのサマリーをセットする
     * @param preference
     * @param theme
     */
    private static void setThemePreferenceSummary(Preference preference, CharSequence theme) {
        // サマリーをセットする
        preference.setSummary(SettingsManager.THEME_ENTRIES.get(Integer.parseInt(theme.toString())));
    }

    /**
     * 発信確認に必要なパーミッションを持っているか確認する
     * @param context
     * @return
     */
    public static boolean hasCallConfirmPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS)
                    == PackageManager.PERMISSION_GRANTED
                    && context.checkSelfPermission(Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    /**
     * 発信確認に必要なパーミッションをリクエストする
     * @param activity
     * @param requestCode
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestCallConfirmPermissions(Activity activity, int requestCode) {
        String[] permissions = new String[]{
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.CALL_PHONE,
        };
        activity.requestPermissions(permissions, requestCode);
    }

    /**
     * 指紋認証に必要なパーミッションを持っているか確認する
     * @param context
     * @return
     */
    public static boolean hasFingerprintPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.USE_FINGERPRINT)
                    == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    /**
     * 指紋認証に必要なパーミッションをリクエストする
     * @param activity
     * @param requestCode
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestFingerprintPermissions(Activity activity, int requestCode) {
        String[] permissions = new String[]{
                Manifest.permission.USE_FINGERPRINT,
        };
        activity.requestPermissions(permissions, requestCode);
    }

    /**
     * 端末が指紋認証機能を持っているかを返します
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isFingerprintHardwareDetected(Context context) {
        // 指紋認証に必要な権限を確認
        if (context.checkSelfPermission(Manifest.permission.USE_FINGERPRINT)
                == PackageManager.PERMISSION_GRANTED) {
            FingerprintManager fingerprintManager =
                    (FingerprintManager) context.getSystemService(Activity.FINGERPRINT_SERVICE);
            // 端末が指紋認証機能を持っているか確認
            if (fingerprintManager.isHardwareDetected()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 有効な指紋が登録されているかを返します
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean hasEnrolledFingerprints(Context context) {
        // 指紋認証に必要な権限を確認
        if (context.checkSelfPermission(Manifest.permission.USE_FINGERPRINT)
                == PackageManager.PERMISSION_GRANTED) {
            FingerprintManager fingerprintManager =
                    (FingerprintManager) context.getSystemService(Activity.FINGERPRINT_SERVICE);
            if (fingerprintManager.hasEnrolledFingerprints()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 指紋認証に必要なパーミッションを持っているか確認する
     * @param context
     * @return
     */
    public static boolean hasBluetoothPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.BLUETOOTH)
                    == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    /**
     * 指紋認証に必要なパーミッションをリクエストする
     * @param activity
     * @param requestCode
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestBluetoothPermissions(Activity activity, int requestCode) {
        String[] permissions = new String[]{
                Manifest.permission.BLUETOOTH,
        };
        activity.requestPermissions(permissions, requestCode);
    }

    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        initCallConfirmEnabledPreference();
        initThemePreference();
        initFingerprintConfirmPreference();
        initBluetoothEnabledPreference();
        initAddBluetoothDevicePreference();
        initRemoveBluetoothDevicePreference();
    }

    /**
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();
        refreshDisplay();
    }

    /**
     * 画面をリフレッシュします
     */
    @Override
    public void refreshDisplay() {
        Context context = getActivity().getApplicationContext();

        SwitchPreference callConfirmEnabledPref =
                (SwitchPreference) findPreference(SettingsManager.PREF_CALL_CONFIRM_ENABLED);
        callConfirmEnabledPref.setChecked(SettingsManager.isCallConfirmEnabled(context));

        ListPreference themePref = (ListPreference) findPreference(SettingsManager.PREF_THEME);
        String theme = SettingsManager.getTheme(context);
        themePref.setValue(theme);
        setThemePreferenceSummary(themePref, theme);

        SwitchPreference fingerprintConfirmPref =
                (SwitchPreference) findPreference(SettingsManager.PREF_FINGERPRINT_CONFIRM);
        fingerprintConfirmPref.setChecked(SettingsManager.isFingerprintConfirm(context));

        SwitchPreference bluetoothEnabledPref =
                (SwitchPreference) findPreference(SettingsManager.PREF_BLUETOOTH_ENABLED);
        bluetoothEnabledPref.setChecked(SettingsManager.isBluetoothEnabled(context));
    }

    /**
     * onRequestPermissionsResult
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Context context = getActivity().getApplicationContext();
        switch (requestCode) {
            case REQUEST_CODE_REQUEST_PERMISSIONS:
                for (int i = 0; i < 2; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        // パーミッションがないのでSimpleCallConfirmを無効にする
                        SettingsManager.setCallConfirmEnabled(context, false);
                        Toast.makeText(context,
                                R.string.disable_confirm_message, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                SettingsManager.setCallConfirmEnabled(context, true);
                SwitchPreference callConfirmEnabledPref =
                        ((SwitchPreference) findPreference(SettingsManager.PREF_CALL_CONFIRM_ENABLED));
                callConfirmEnabledPref.setChecked(true);
                break;
            case REQUEST_CODE_REQUEST_FINGERPRINT_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // パーミッションがないので指紋認証を無効にする
                    SettingsManager.setFingerprintConfirm(context, false);
                    Toast.makeText(context,
                            R.string.disable_fingerprint_confirm_message, Toast.LENGTH_LONG).show();
                    return;
                }
                SettingsManager.setFingerprintConfirm(context, true);
                SwitchPreference fingerprintConfirmPref =
                        ((SwitchPreference) findPreference(SettingsManager.PREF_FINGERPRINT_CONFIRM));
                fingerprintConfirmPref.setChecked(true);
                break;
            case REQUEST_CODE_REQUEST_BLUETOOTH_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // パーミッションがないのでBluetoothを無効にする
                    SettingsManager.setBluetoothEnabled(context, false);
                    Toast.makeText(context,
                            R.string.disable_bluetooth_message, Toast.LENGTH_LONG).show();
                    return;
                }
                SettingsManager.setBluetoothEnabled(context, true);
                SwitchPreference bluetoothEnabledPref =
                        ((SwitchPreference) findPreference(SettingsManager.PREF_BLUETOOTH_ENABLED));
                bluetoothEnabledPref.setChecked(true);

                if (SettingsManager.getBluetoothDevices(context).size() < 1) {
                    DialogFragment dialogFragment = new AddBluetoothDeviceListFragment();
                    dialogFragment.show(getFragmentManager(), DIALOG_ADD_BLUETOOTH_DEVICE_LIST);
                }

                break;
        }
    }

    /**
     * 発信確認設定の初期化
     */
    private void initCallConfirmEnabledPreference() {
        final Context context = getActivity().getApplicationContext();
        SwitchPreference callConfirmEnabledPref =
                (SwitchPreference) findPreference(SettingsManager.PREF_CALL_CONFIRM_ENABLED);

        callConfirmEnabledPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    // 発信確認に必要なパーミッションの確認
                    if (!hasCallConfirmPermissions(context)) {
                        // 発信確認に必要なパーミッションをリクエスト
                        requestCallConfirmPermissions(getActivity(), REQUEST_CODE_REQUEST_PERMISSIONS);
                        return false;
                    }
                }

                // Widgetを更新
                Intent intent = new Intent();
                intent.setAction(MainWidgetUpdateReceiver.ACTION);
                context.sendBroadcast(intent);

                return true;
            }
        });
    }

    /**
     * テーマ設定を初期化する
     */
    private void initThemePreference() {
        ListPreference themePref = (ListPreference) findPreference(SettingsManager.PREF_THEME);
        CharSequence[] entries = new CharSequence[SettingsManager.THEME_ENTRIES.size()];
        CharSequence[] entryValues = new CharSequence[SettingsManager.THEME_ENTRIES.size()];
        int i = 0;
        Iterator<Integer> iterator = SettingsManager.THEME_ENTRIES.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            entries[i] = SettingsManager.THEME_ENTRIES.get(key);
            entryValues[i++] = String.valueOf(key);
        }
        themePref.setEntries(entries);
        themePref.setEntryValues(entryValues);
        themePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                CharSequence theme = (CharSequence) newValue;
                setThemePreferenceSummary(preference, theme);
                // SettingsActivityを再起動してテーマを反映する
                Activity activity = getActivity();
                startActivity(new Intent(activity, SettingsActivity.class));
                activity.finish();
                return true;
            }
        });
    }

    /**
     * 指紋認証設定の初期化
     */
    private void initFingerprintConfirmPreference() {
        final Context context = getActivity().getApplicationContext();
        final SwitchPreference fingerprintConfirmPref =
                (SwitchPreference) findPreference(SettingsManager.PREF_FINGERPRINT_CONFIRM);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 端末が指紋認証機能をもっているかチェック
            if (isFingerprintHardwareDetected(context)) {
                fingerprintConfirmPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if ((boolean) newValue) {
                            // ----- 有効にする -----
                            // 指紋認証に必要なパーミッションの確認
                            if (!hasFingerprintPermissions(context)) {
                                requestFingerprintPermissions(
                                        getActivity(), REQUEST_CODE_REQUEST_FINGERPRINT_PERMISSIONS);
                                return false;
                            }

                            // 有効な指紋が登録されているか確認
                            if (!hasEnrolledFingerprints(context)) {
                                Toast.makeText(context, R.string.not_has_enrolled_fingerprints,
                                        Toast.LENGTH_LONG).show();
                                return false;
                            }
                        } else {
                            // ----- 無効にする -----
                            // 無効にする前に指紋認証する
                            final FingerprintDialogFragment dialog =
                                    getFingerprintDialogFragment(fingerprintConfirmPref);
                            dialog.show(getFragmentManager(), "fingerPrintDialog");

                            return false;
                        }

                        return true;
                    }
                });
            } else {
                // 指紋認証機能を無効にする
                fingerprintConfirmPref.setEnabled(false);
                fingerprintConfirmPref.setSummaryOff(R.string.fingerprint_not_supported);
            }
        } else {
            // 指紋認証機能を無効にする
            fingerprintConfirmPref.setEnabled(false);
            fingerprintConfirmPref.setSummaryOff(R.string.fingerprint_not_supported);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private FingerprintDialogFragment getFingerprintDialogFragment(final SwitchPreference fingerprintConfirmPref) {
        final FingerprintDialogFragment dialog = new FingerprintDialogFragment();
        dialog.setTitle(getString(R.string.disable_fingerprint_dialog_title));
        dialog.setMessage(getString(R.string.disable_fingerprint_dialog_message));
        dialog.setCallback(new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                dialog.dismiss();
            }

            @Override
            public void onAuthenticationFailed() {
                dialog.dismiss();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                fingerprintConfirmPref.setChecked(false);
                dialog.dismiss();
            }
        });

        return dialog;
    }
    /**
     * Bluetooth設定を初期化する
     */
    private void initBluetoothEnabledPreference() {
        final Context context = getActivity().getApplicationContext();
        SwitchPreference bluetoothEnabledPref =
                (SwitchPreference)findPreference(SettingsManager.PREF_BLUETOOTH_ENABLED);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // 端末がBluetoothをサポートしていなければ無効にする
            bluetoothEnabledPref.setEnabled(false);
            bluetoothEnabledPref.setSummaryOff(R.string.bluetooth_not_supported);
            return;
        }

        bluetoothEnabledPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // Bluetoothに必要なパーミッションを確認する
                        if (!hasBluetoothPermissions(context)) {
                            // Bluetoothに必要なパーミッションをリクエストする
                            requestBluetoothPermissions(getActivity(), REQUEST_CODE_REQUEST_BLUETOOTH_PERMISSIONS);
                            return false;
                        }
                    }

                    if (SettingsManager.getBluetoothDevices(context).size() < 1) {
                        DialogFragment dialogFragment = new AddBluetoothDeviceListFragment();
                        dialogFragment.show(getFragmentManager(), DIALOG_ADD_BLUETOOTH_DEVICE_LIST);
                    }
                }
                return true;
            }
        });
    }

    private void initAddBluetoothDevicePreference() {
        Preference addBluetoothDevicePref = findPreference(PREF_ADD_BLUETOOTH_DEVICE);
        addBluetoothDevicePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment dialogFragment = new AddBluetoothDeviceListFragment();
                dialogFragment.show(getFragmentManager(), DIALOG_ADD_BLUETOOTH_DEVICE_LIST);
                return true;
            }
        });
    }

    private void initRemoveBluetoothDevicePreference() {
        Preference removeBluetoothDevicePref = findPreference(PREF_REMOVE_BLUETOOTH_DEVICE);
        removeBluetoothDevicePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment dialogFragment = new RemoveBluetoothDeviceListFragment();
                dialogFragment.show(getFragmentManager(), DIALOG_REMOVE_BLUETOOTH_DEVICE_LIST);
                return true;
            }
        });
    }
}
