package com.tvxargtec.online.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.PlayAty;

public class FavoritesWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_favorites);

            Intent serviceIntent = new Intent(context, FavoritesWidgetService.class);
            views.setRemoteAdapter(R.id.widget_list, serviceIntent);

            Intent clickIntent = new Intent(context, PlayAty.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setPendingIntentTemplate(R.id.widget_list, pendingIntent);

            views.setEmptyView(R.id.widget_list, android.R.id.empty);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
