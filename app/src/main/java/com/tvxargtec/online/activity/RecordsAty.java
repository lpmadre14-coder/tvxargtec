package com.tvxargtec.online.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.utils.ChannelItem;
import com.tvxargtec.online.utils.LocalDataManager;

import java.util.List;

public class RecordsAty extends BaseActivity {

    private ListView listView;
    private TextView tvEmpty;
    private LocalDataManager dataManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_records;
    }

    @Override
    protected void initView() {
        listView = findViewById(R.id.listView);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    @Override
    protected void initData() {
        dataManager = new LocalDataManager(this);
        loadHistory();
    }

    private void loadHistory() {
        List<ChannelItem> history = dataManager.getHistory();
        if (history.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            listView.setVisibility(android.view.View.GONE);
            return;
        }
        tvEmpty.setVisibility(android.view.View.GONE);
        listView.setVisibility(android.view.View.VISIBLE);

        String[] titles = history.stream().map(c -> c.title).toArray(String[]::new);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            ChannelItem item = history.get(position);
            Intent intent = new Intent(this, PlayAty.class);
            intent.putExtra("url", item.url);
            intent.putExtra("title", item.title);
            startActivity(intent);
        });
    }
}
