package com.gmail.okumura.android.simplecallconfirm2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by naoki on 15/12/27.
 */
public class MainSettingsFragment extends PreferenceFragment implements RefreshDisplayInterface {
    private static final int REQUEST_CODE_REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_CODE_REQUEST_FINGERPRINT_PERMISSIONS = 2;
    private static final int REQUEST_CODE_REQUEST_BLUETOOTH_PERMISSIONS = 3;

    private static final String PREF_CALL_CONFIRM_ENABLED = "call_confirm_enabled";
    private static final String PREF_THEME = "theme";
    private static final String PREF_FINGERPRINT_CONFIRM = "fingerprint_confirm";
    private static final String PREF_BLUETOOTH_ENABLED = "bluetooth_enabled";
    private static final String PREF_ADD_BLUETOOTH_DEVICE = "add_bluetooth_device";
    private static final String PREF_DELETE_BLUETOOTH_DEVICE = "delete_bluetooth_device";
    private static final String PREF_BLUETOOTH_DEVICES = "bluetooth_devices";

    private static final String DIALOG_ADD_BLUETOOTH_DEVICE_LIST = "add_bluetooth_device_list";
    private static final String DIALOG_DELETE_BLUETOOTH_DEVICE_LIST = "delete_bluetooth_device_list";

    private static final CharSequence[] THEME_ENTRIES = new CharSequence[] {
            "DeviceDefault",
            "DeviceDefault Light",
            "Material",
            "Material Light",
            "Holo",
            "Holo Light",
            "Default",
    };

    private static final CharSequence[] THEME_ENTRY_VALUES = new CharSequence[] {
            String.valueOf(android.R.style.Theme_DeviceDefault),
            String.valueOf(android.R.style.Theme_DeviceDefault_Light),
            String.valueOf(android.R.style.Theme_Material),
            String.valueOf(android.R.style.Theme_Material_Light),
            String.valueOf(android.R.style.Theme_Holo),
            String.valueOf(android.R.style.Theme_Holo_Light),
            String.valueOf(android.R.style.Theme),
    };

    public static int getIntTheme(Context context) {
        return Integer.valueOf(getTheme(context));
    }

    public static String getTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_THEME, String.valueOf(android.R.style.Theme_DeviceDefault));
    }

    public static boolean isCallConfirmEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_CALL_CONFIRM_ENABLED, true);
    }

    public static void setCallConfirmEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_CALL_CONFIRM_ENABLED, enabled).commit();
    }

    public static boolean isFingerprintConfirm(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_FINGERPRINT_CONFIRM, false);
    }

    public static void setFingerprintConfirm(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_FINGERPRINT_CONFIRM, enabled).commit();
    }

    public static boolean isBluetoothEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_BLUETOOTH_ENABLED, false);
    }

    public static void setBluetoothEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_BLUETOOTH_ENABLED, enabled).commit();
    }

    public static void addBluetoothDevice(Context context, String address) {
        Set<String> addressSet = getBluetoothDevices(context);
        addressSet.add(address);
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putStringSet(PREF_BLUETOOTH_DEVICES, addressSet).commit();
    }

    public static int deleteBluetoothDevice(Context context, String address) {
        Set<String> addressSet = getBluetoothDevices(context);
        for (String addr : addressSet) {
            if (addr.equals(address)) {
                addressSet.remove(addr);
                break;
            }
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putStringSet(PREF_BLUETOOTH_DEVICES, addressSet).commit();

        return addressSet.size();
    }

    public static Set<String> getBluetoothDevices(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                        .getStringSet(PREF_BLUETOOTH_DEVICES, new HashSet<String>());
    }

    /**
     * テーマのサマリーをセットする
     * @param preference
     * @param theme
     */
    private static void setThemePreferenceSummary(Preference preference, String theme) {
        // 現在選択されているテーマのキーを取得する
        int key = 0;
        for (int i = 0; i < THEME_ENTRY_VALUES.length; i++) {
            if (theme.equals(THEME_ENTRY_VALUES[i])) {
                key = i;
                break;
            }
        }
        // サマリーをセットする
        preference.setSummary(THEME_ENTRIES[key]);
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
        String[] permissions = new String[] {
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
        String[] permissions = new String[] {
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
        String[] permissions = new String[] {
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
        initDeleteBluetoothDevicePreference();
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
                (SwitchPreference)findPreference(PREF_CALL_CONFIRM_ENABLED);
        callConfirmEnabledPref.setChecked(isCallConfirmEnabled(context));

        ListPreference themePref = (ListPreference)findPreference(PREF_THEME);
        String theme = getTheme(context);
        themePref.setValue(theme);
        setThemePreferenceSummary(themePref, theme);

        SwitchPreference fingerprintConfirmPref =
                (SwitchPreference)findPreference(PREF_FINGERPRINT_CONFIRM);
        fingerprintConfirmPref.setChecked(isFingerprintConfirm(context));

        SwitchPreference bluetoothEnabledPref = (SwitchPreference)findPreference(PREF_BLUETOOTH_ENABLED);
        bluetoothEnabledPref.setChecked(isBluetoothEnabled(context));
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
                        setCallConfirmEnabled(context, false);
                        Toast.makeText(context, R.string.disable_confirm_message, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                setCallConfirmEnabled(context, true);
                SwitchPreference callConfirmEnabledPref =
                        ((SwitchPreference)findPreference(PREF_CALL_CONFIRM_ENABLED));
                callConfirmEnabledPref.setChecked(true);
                break;
            case REQUEST_CODE_REQUEST_FINGERPRINT_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // パーミッションがないので指紋認証を無効にする
                    setFingerprintConfirm(context, false);
                    Toast.makeText(context, R.string.disable_fingerprint_confirm_message, Toast.LENGTH_LONG).show();
                    return;
                }
                setFingerprintConfirm(context, true);
                SwitchPreference fingerprintConfirmPref =
                        ((SwitchPreference)findPreference(PREF_FINGERPRINT_CONFIRM));
                fingerprintConfirmPref.setChecked(true);
                break;
            case REQUEST_CODE_REQUEST_BLUETOOTH_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // パーミッションがないのでBluetoothを無効にする
                    setBluetoothEnabled(context, false);
                    Toast.makeText(context, R.string.disable_bluetooth_message, Toast.LENGTH_LONG).show();
                    return;
                }
                setBluetoothEnabled(context, true);
                SwitchPreference bluetoothEnabledPref =
                        ((SwitchPreference)findPreference(PREF_BLUETOOTH_ENABLED));
                bluetoothEnabledPref.setChecked(true);

                if (MainSettingsFragment.getBluetoothDevices(context).size() < 1) {
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
                (SwitchPreference)findPreference(PREF_CALL_CONFIRM_ENABLED);

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
                return true;
            }
        });
    }

    /**
     * テーマ設定を初期化する
     */
    private void initThemePreference() {
        ListPreference themePref = (ListPreference)findPreference(PREF_THEME);
        themePref.setEntries(THEME_ENTRIES);
        themePref.setEntryValues(THEME_ENTRY_VALUES);
        themePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String theme = (String) newValue;
                setThemePreferenceSummary(preference, theme);
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
                (SwitchPreference)findPreference(PREF_FINGERPRINT_CONFIRM);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 端末が指紋認証機能をもっているかチェック
            if (isFingerprintHardwareDetected(context)) {
                fingerprintConfirmPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if ((boolean)newValue) {
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
//                            FingerprintManager fingerprintManager =
//                                    (FingerprintManager)context.getSystemService(Activity.FINGERPRINT_SERVICE);
//                            // TODO ここが動作しない
//                            fingerprintManager.authenticate(null, null, 0, new FingerprintManager.AuthenticationCallback() {
//                                @Override
//                                public void onAuthenticationError(int errorCode, CharSequence errString) {
//                                }
//
//                                @Override
//                                public void onAuthenticationFailed() {
//                                }
//
//                                @Override
//                                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
//                                    setFingerprintConfirm(context, false);
//                                    fingerprintConfirmPref.setEnabled(false);
//                                }
//                            }, new Handler());
//
//                            return false;
                        }

                        return true;
                    }
                });
            } else {
                // 指紋認証機能を無効にする
                fingerprintConfirmPref.setEnabled(false);
            }
        } else {
            // 指紋認証機能を無効にする
            fingerprintConfirmPref.setEnabled(false);
        }
    }

    /**
     * Bluetooth設定を初期化する
     */
    private void initBluetoothEnabledPreference() {
        final Context context = getActivity().getApplicationContext();
        SwitchPreference bluetoothEnabledPref = (SwitchPreference)findPreference(PREF_BLUETOOTH_ENABLED);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // 端末がBluetoothをサポートしていなければ無効にする
            bluetoothEnabledPref.setEnabled(false);
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

                    if (MainSettingsFragment.getBluetoothDevices(context).size() < 1) {
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

    private void initDeleteBluetoothDevicePreference() {
        Preference deleteBluetoothDevicePref = findPreference(PREF_DELETE_BLUETOOTH_DEVICE);
        deleteBluetoothDevicePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment dialogFragment = new DeleteBluetoothDeviceListFragment();
                dialogFragment.show(getFragmentManager(), DIALOG_DELETE_BLUETOOTH_DEVICE_LIST);
                return true;
            }
        });
    }
}
