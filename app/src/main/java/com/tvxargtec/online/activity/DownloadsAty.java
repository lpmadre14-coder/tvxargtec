package com.tvxargtec.online.activity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.utils.OfflineManager;

import java.util.ArrayList;
import java.util.List;

public class DownloadsAty extends BaseActivity {

    private ListView listView;
    private TextView tvEmpty;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_downloads;
    }

    @Override
    protected void initView() {
        listView = findViewById(R.id.listView);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    @Override
    protected void initData() {
        OfflineManager manager = OfflineManager.Companion.getInstance(this);
        var downloads = manager.getDownloads();

        if (downloads.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            listView.setVisibility(android.view.View.GONE);
            return;
        }

        tvEmpty.setVisibility(android.view.View.GONE);
        listView.setVisibility(android.view.View.VISIBLE);

        List<String> items = new ArrayList<>();
        for (var download : downloads) {
            String title = new String(download.request.data != null ? download.request.data : new byte[0]);
            String status = downloadStateToString(download.state);
            items.add((title.isEmpty() ? "Contenido" : title) + " - " + status);
        }

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }

    private String downloadStateToString(int state) {
        switch (state) {
            case androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING:
                return "Descargando...";
            case androidx.media3.exoplayer.offline.Download.STATE_COMPLETED:
                return "Completado";
            case androidx.media3.exoplayer.offline.Download.STATE_FAILED:
                return "Error";
            default:
                return "Pendiente";
        }
    }
}
