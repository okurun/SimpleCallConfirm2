package com.gmail.okumura.android.simplecallconfirm2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddBluetoothDeviceListFragment extends DialogFragment {
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();

    /**
     * コンストラクター
     */
    public AddBluetoothDeviceListFragment() {
    }

    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Set<String> targetDevices = MainSettingsFragment.getBluetoothDevices(activity);
        for (BluetoothDevice device : pairedDevices) {
            if (targetDevices.contains(device.getAddress())) {
                // すでに登録されているものは表示しない
                continue;
            }
            // TODO ヘッドセットのみに絞り込む

            mDeviceList.add(device);
        }

        if (mDeviceList.size() < 1) {
            Toast.makeText(activity, R.string.bluetooth_device_not_found, Toast.LENGTH_LONG).show();
            dismiss();
        }
    }

    /**
     * onCreateDialog
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] items = new CharSequence[mDeviceList.size()];
        int i = 0;
        for (BluetoothDevice device : mDeviceList) {
            items[i++] = device.getName();
        }

        final Context context = getActivity().getApplicationContext();
        int style = MainSettingsFragment.getIntTheme(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), style));
        builder.setTitle(R.string.add_bluetooth_device_dialog_title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BluetoothDevice device = mDeviceList.get(which);
                if (null != device) {
                    MainSettingsFragment.addBluetoothDevice(context, device.getAddress());
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    /**
     * onCancel
     * @param dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        Context context = getActivity().getApplicationContext();
        if (MainSettingsFragment.getBluetoothDevices(context).size() < 1) {
            // 機器が登録されていなければBluetoothを無効にする
            MainSettingsFragment.setBluetoothEnabled(context, false);
            RefreshDisplayInterface refreshDisplay =
                    (RefreshDisplayInterface)getFragmentManager().findFragmentById(android.R.id.content);
            refreshDisplay.refreshDisplay();
        }
    }
}
