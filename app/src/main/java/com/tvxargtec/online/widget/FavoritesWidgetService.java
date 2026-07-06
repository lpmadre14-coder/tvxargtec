package com.tvxargtec.online.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.tvxargtec.online.R;
import com.tvxargtec.online.database.AppDatabase;
import com.tvxargtec.online.database.entity.FavoriteEntity;
import com.tvxargtec.online.utils.Channel;
import com.tvxargtec.online.utils.ChannelDataManager;

import java.util.ArrayList;
import java.util.List;

public class FavoritesWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FavoritesRemoteViewsFactory(getApplicationContext());
    }

    private static class FavoritesRemoteViewsFactory implements RemoteViewsFactory {

        private final Context context;
        private final List<ItemData> items = new ArrayList<>();

        private static class ItemData {
            final String title;
            final String url;
            final String logo;

            ItemData(String title, String url, String logo) {
                this.title = title;
                this.url = url;
                this.logo = logo;
            }
        }

        FavoritesRemoteViewsFactory(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate() {
            loadData();
        }

        @Override
        public void onDataSetChanged() {
            loadData();
        }

        private void loadData() {
            items.clear();
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                List<FavoriteEntity> favEntities = db.favoriteDao().getAllFavorites();
                List<Channel> channels = ChannelDataManager.getChannels(context);

                for (FavoriteEntity fav : favEntities) {
                    for (Channel ch : channels) {
                        if (ch.getId().equals(fav.contentId)) {
                            items.add(new ItemData(
                                    ch.getTitle() != null ? ch.getTitle() : "",
                                    ch.getUrl() != null ? ch.getUrl() : "",
                                    ch.getLogo() != null ? ch.getLogo() : ""));
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) {
                return new RemoteViews(context.getPackageName(), R.layout.widget_favorites_item);
            }

            ItemData item = items.get(position);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_favorites_item);
            views.setTextViewText(R.id.widget_item_title, item.title);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("url", item.url);
            fillInIntent.putExtra("title", item.title);
            views.setOnClickFillInIntent(R.id.widget_item_title, fillInIntent);
            views.setOnClickFillInIntent(R.id.widget_item_logo, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void onDestroy() {
            items.clear();
        }
    }
}
