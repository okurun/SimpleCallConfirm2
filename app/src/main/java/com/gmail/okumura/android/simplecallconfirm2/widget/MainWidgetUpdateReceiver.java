package com.gmail.okumura.android.simplecallconfirm2.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.gmail.okumura.android.simplecallconfirm2.R;
import com.gmail.okumura.android.simplecallconfirm2.settings.SettingsManager;

/**
 * Created by naoki on 16/01/10.
 */
public class MainWidgetUpdateReceiver extends BroadcastReceiver {
    public static final String ACTION = "UPDATE_SIMPLE_CALL_CONFIRM_MAIN_WIDGET";
    public static final String EXTRAS_TOGGLE_ENABLE_CALL_CONFIRM = "toggle_enable_call_confirm";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main);

            if (intent.getBooleanExtra(EXTRAS_TOGGLE_ENABLE_CALL_CONFIRM, false)) {
                // CallConfirmの有効・無効を反転させる
                SettingsManager.setCallConfirmEnabled(context, !SettingsManager.isCallConfirmEnabled(context));
            }

            // ボタン画像を変更する
            if (SettingsManager.isCallConfirmEnabled(context)) {
                remoteViews.setViewVisibility(R.id.widget_disable_icon, View.GONE);
            } else {
                remoteViews.setViewVisibility(R.id.widget_disable_icon, View.VISIBLE);
            }

            // ボタンを再度設定しなおす
            remoteViews.setOnClickPendingIntent(R.id.widget_main_button, MainWidgetProvider.clickMainButton(context));
            MainWidgetProvider.pushWidgetUpdate(context, remoteViews);
        }
    }
}