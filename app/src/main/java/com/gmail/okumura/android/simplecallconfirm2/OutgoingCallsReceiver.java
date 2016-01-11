package com.gmail.okumura.android.simplecallconfirm2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.gmail.okumura.android.simplecallconfirm2.settings.MainSettingsFragment;
import com.gmail.okumura.android.simplecallconfirm2.settings.SettingsManager;

import java.util.Set;

/**
 * Created by naoki on 15/12/27.
 */
public class OutgoingCallsReceiver extends BroadcastReceiver {
    public static final String SCHEME_TEL = "tel:";

    private static boolean sIgnoreConfirm = false;

    /**
     * trueをセットすると１度だけ発信確認を表示しない
     * @param ignoreConfirm
     */
    public static void setIgnoreConfirm(boolean ignoreConfirm) {
        sIgnoreConfirm = ignoreConfirm;
    }

    private BluetoothAdapter mBluetoothAdapter;

    /**
     * onReceive
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (sIgnoreConfirm) {
            sIgnoreConfirm = false;
            return;
        }

        // 発信確認が有効か確認
        if (!SettingsManager.isCallConfirmEnabled(context)) {
            return;
        }

        // Bluetoothが有効か確認
        if (SettingsManager.isBluetoothEnabled(context)) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            if (profile == BluetoothProfile.HEADSET) {
                                BluetoothHeadset mBluetoothHeadset = (BluetoothHeadset) proxy;
                                Set<String> addressSet = SettingsManager.getBluetoothDevices(context);
                                for (BluetoothDevice device : mBluetoothHeadset.getConnectedDevices()) {
                                    if (addressSet.contains(device.getAddress())) {
                                        call(context, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
                                        return;
                                    }
                                }
                                showCallConfirm(context, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
                            }
                        }

                        public void onServiceDisconnected(int profile) {
                        }
                    }, BluetoothProfile.HEADSET);

                    // ブロードキャストをキャンセルする
                    abortBroadcast();
                    setResultData(null);
                    return;
                }
            }
        }

        // ブロードキャストをキャンセルする
        abortBroadcast();
        setResultData(null);

        // 発信確認ダイアログを表示する
        showCallConfirm(context, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
    }

    private void showCallConfirm(Context context, String number) {
        Intent i = new Intent(context, ConfirmActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Intent.EXTRA_PHONE_NUMBER, number);
        context.startActivity(i);
    }

    /**
     * 電話を発信します
     */
    private void call(Context context, String number) {
        // 電話の発信権限があるか確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && context.checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO 権限がないので発信できない
            return;
        }

        OutgoingCallsReceiver.setIgnoreConfirm(true);

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(SCHEME_TEL + number));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
