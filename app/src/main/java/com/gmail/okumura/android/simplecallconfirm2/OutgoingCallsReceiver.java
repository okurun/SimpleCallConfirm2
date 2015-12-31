package com.gmail.okumura.android.simplecallconfirm2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by naoki on 15/12/27.
 */
public class OutgoingCallsReceiver extends BroadcastReceiver {
    private static boolean sIgnoreConfirm = false;

    public static void setIgnoreConfirm(boolean ignoreConfirm) {
        sIgnoreConfirm = ignoreConfirm;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (sIgnoreConfirm) {
            sIgnoreConfirm = false;
            return;
        }

        if (!MainSettingsFragment.isCallConfirmEnabled(context)) {
            return;
        }

        abortBroadcast();
        setResultData(null);

        Intent i = new Intent(context, ConfirmActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Intent.EXTRA_PHONE_NUMBER, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
        context.startActivity(i);
    }
}
