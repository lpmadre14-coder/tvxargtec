package com.tvxargtec.online.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.adapter.DownloadsAdapter;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.utils.OfflineManager;

public class DownloadsAty extends BaseActivity {

    private RecyclerView rvDownloads;
    private TextView tvEmpty;
    private DownloadsAdapter adapter;
    private OfflineManager offlineManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_downloads;
    }

    @Override
    protected void initView() {
        rvDownloads = findViewById(R.id.rvDownloads);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    @Override
    protected void initData() {
        offlineManager = OfflineManager.Companion.getInstance(this);
        rvDownloads.setLayoutManager(new LinearLayoutManager(this));
        var downloads = offlineManager.getDownloads();
        adapter = new DownloadsAdapter(this, downloads);
        rvDownloads.setAdapter(adapter);

        if (downloads.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            rvDownloads.setVisibility(android.view.View.GONE);
        } else {
            tvEmpty.setVisibility(android.view.View.GONE);
            rvDownloads.setVisibility(android.view.View.VISIBLE);
        }
    }
}
