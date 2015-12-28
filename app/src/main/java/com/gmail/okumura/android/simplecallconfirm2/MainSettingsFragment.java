package com.gmail.okumura.android.simplecallconfirm2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

/**
 * Created by naoki on 15/12/27.
 */
public class MainSettingsFragment extends PreferenceFragment {
    private static final int REQUEST_CODE_REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_CODE_REQUEST_FINGERPRINT_PERMISSIONS = 2;

    private static final String PREF_ENABLED = "enabled";
    private static final String PREF_FINGERPRINT_CONFIRM = "fingerprint_confirm";
    private static final String PREF_THEME = "theme";

    public static boolean isEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_ENABLED, enabled).commit();
    }

    public static boolean isFingerprintConfirm(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_FINGERPRINT_CONFIRM, false);
    }

    public static void setFingerprintConfirm(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_FINGERPRINT_CONFIRM, enabled).commit();
    }

    public static int getTheme(Context context) {
        return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_THEME, String.valueOf(android.R.style.Theme_Holo_Dialog)));
    }

    public static String getThemeStr(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_THEME, String.valueOf(android.R.style.Theme_Holo_Dialog));
    }

    public static void setThme(Context context, String theme) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREF_THEME, theme).commit();
    }

    private static final CharSequence[] THEME_ENTRIES = new CharSequence[] {
            "Holo",
            "Holo Light",
            "DeviceDefault",
            "DeviceDefault Light",
            "Material",
            "Material Light",
            "No Theme",
    };

    private static final CharSequence[] THEME_ENTRY_VALUES = new CharSequence[] {
            String.valueOf(android.R.style.Theme_Holo_Dialog),
            String.valueOf(android.R.style.Theme_Holo_Light_Dialog),
            String.valueOf(android.R.style.Theme_DeviceDefault_Dialog),
            String.valueOf(android.R.style.Theme_DeviceDefault_Light_Dialog),
            String.valueOf(android.R.style.Theme_Material_Dialog),
            String.valueOf(android.R.style.Theme_Material_Light_Dialog),
            String.valueOf(android.R.style.Theme_Dialog),
    };

    private static void setThemePrefSummary(Preference preference, String theme) {
        int key = 0;
        for (int i = 0; i < THEME_ENTRY_VALUES.length; i++) {
            if (theme.equals(THEME_ENTRY_VALUES[i])) {
                key = i;
                break;
            }
        }
        preference.setSummary(THEME_ENTRIES[key]);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean hasPermissions(Context context) {
        return context.checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        initEnabledPreference();
        initFingerprintConfirmPreference();
        initThemePreference();
    }

    private void initEnabledPreference() {
        final Context context = getActivity().getApplicationContext();

        SwitchPreference enabledPref = (SwitchPreference)findPreference(PREF_ENABLED);
        enabledPref.setChecked(isEnabled(context));
        enabledPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!hasPermissions(context)) {
                            String[] permissions = new String[]{
                                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                                    Manifest.permission.CALL_PHONE,
                            };
                            requestPermissions(permissions, REQUEST_CODE_REQUEST_PERMISSIONS);
                            return false;
                        }
                    }
                }
                return true;
            }
        });
    }

    private void initFingerprintConfirmPreference() {
        final Context context = getActivity().getApplicationContext();

        SwitchPreference fingerprintConfirmPref =
                (SwitchPreference)findPreference(PREF_FINGERPRINT_CONFIRM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final FingerprintManager fingerprintManager =
                    (FingerprintManager)context.getSystemService(Activity.FINGERPRINT_SERVICE);
            if (fingerprintManager.isHardwareDetected()) {
                fingerprintConfirmPref.setChecked(isFingerprintConfirm(context));
                fingerprintConfirmPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if ((boolean)newValue) {
                            if (context.checkSelfPermission(Manifest.permission.USE_FINGERPRINT)
                                    != PackageManager.PERMISSION_GRANTED) {
                                String[] permissions = new String[]{Manifest.permission.USE_FINGERPRINT};
                                requestPermissions(permissions, REQUEST_CODE_REQUEST_FINGERPRINT_PERMISSIONS);
                                return false;
                            }
                            if (!fingerprintManager.hasEnrolledFingerprints()) {
                                Toast.makeText(context, R.string.not_has_enrolled_fingerprints, Toast.LENGTH_LONG).show();
                                return false;
                            }
                        }
                        return true;
                    }
                });
            } else {
                fingerprintConfirmPref.setEnabled(false);
            }
        } else {
            fingerprintConfirmPref.setEnabled(false);
        }
    }

    private void initThemePreference() {
        Context context = getActivity().getApplicationContext();
        String theme = getThemeStr(context);

        ListPreference themePref = (ListPreference)findPreference(PREF_THEME);
        themePref.setEntries(THEME_ENTRIES);
        themePref.setEntryValues(THEME_ENTRY_VALUES);
        themePref.setValue(theme);
        setThemePrefSummary(themePref, theme);
        themePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String theme = (String) newValue;
                setThemePrefSummary(preference, theme);
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Context context = getActivity().getApplicationContext();
        switch (requestCode) {
            case REQUEST_CODE_REQUEST_PERMISSIONS:
                for (int i = 0; i < 2; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        setEnabled(context, false);
                        Toast.makeText(context, R.string.disable_confirm_message, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                setEnabled(context, true);
                CheckBoxPreference enabledPref = ((CheckBoxPreference) findPreference(PREF_ENABLED));
                enabledPref.setChecked(true);
                break;
            case REQUEST_CODE_REQUEST_FINGERPRINT_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    setFingerprintConfirm(context, false);
                    Toast.makeText(context, R.string.disable_fingerprint_confirm_message, Toast.LENGTH_LONG).show();
                    return;
                }
                setFingerprintConfirm(context, true);
                CheckBoxPreference fingerprintConfirmPref =
                        ((CheckBoxPreference)findPreference(PREF_FINGERPRINT_CONFIRM));
                fingerprintConfirmPref.setChecked(true);
                break;
        }
    }
}
