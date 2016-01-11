package com.gmail.okumura.android.simplecallconfirm2.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.gmail.okumura.android.simplecallconfirm2.R;

/**
 * Created by naoki on 16/01/10.
 */
public class MainWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main);
        remoteViews.setOnClickPendingIntent(R.id.widget_main_button, clickMainButton(context));
        pushWidgetUpdate(context, remoteViews);

        Intent intent = new Intent();
        intent.setAction(MainWidgetUpdateReceiver.ACTION);
        context.sendBroadcast(intent);
    }

    public static PendingIntent clickMainButton(Context context) {
        Intent intent = new Intent();
        intent.setAction(MainWidgetUpdateReceiver.ACTION);
        intent.putExtra(MainWidgetUpdateReceiver.EXTRAS_TOGGLE_ENABLE_CALL_CONFIRM, true);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName mainWidgetProvider = new ComponentName(context, MainWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(mainWidgetProvider, remoteViews);
    }
}