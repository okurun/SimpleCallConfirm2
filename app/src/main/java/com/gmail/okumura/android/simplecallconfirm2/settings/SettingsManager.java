package com.gmail.okumura.android.simplecallconfirm2.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

import com.gmail.okumura.android.simplecallconfirm2.R;
import com.gmail.okumura.android.simplecallconfirm2.widget.MainWidgetUpdateReceiver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by naoki on 16/01/11.
 */
public class SettingsManager {
    public static final Map<Integer, CharSequence> THEME_ENTRIES = new TreeMap<>();
    static {
        THEME_ENTRIES.put(android.R.style.Theme, "No Theme");
        THEME_ENTRIES.put(android.R.style.Theme_Holo, "Holo");
        THEME_ENTRIES.put(android.R.style.Theme_Holo_Light, "Holo Light");
        THEME_ENTRIES.put(android.R.style.Theme_DeviceDefault, "DeviceDefault");
        THEME_ENTRIES.put(android.R.style.Theme_DeviceDefault_Light, "DeviceDefault Light");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            THEME_ENTRIES.put(android.R.style.Theme_Material, "Material");
            THEME_ENTRIES.put(android.R.style.Theme_Material_Light, "Material Light");
        }
    }

    public static final String PREF_CALL_CONFIRM_ENABLED = "call_confirm_enabled";
    public static final String PREF_THEME = "app_theme";
    public static final String PREF_FINGERPRINT_CONFIRM = "fingerprint_confirm";
    public static final String PREF_BLUETOOTH_ENABLED = "bluetooth_enabled";
    public static final String PREF_BLUETOOTH_DEVICES = "bluetooth_devices";

    public static boolean isCallConfirmEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_CALL_CONFIRM_ENABLED, true);
    }

    public static void setCallConfirmEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_CALL_CONFIRM_ENABLED, enabled).commit();

        // Widgetを更新
        Intent intent = new Intent();
        intent.setAction(MainWidgetUpdateReceiver.ACTION);
        context.sendBroadcast(intent);
    }

    public static int getIntTheme(Context context) {
        return Integer.valueOf(getTheme(context));
    }

    public static String getTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_THEME, String.valueOf(android.R.style.Theme_DeviceDefault));
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

    public static int removeBluetoothDevice(Context context, String address) {
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

}
