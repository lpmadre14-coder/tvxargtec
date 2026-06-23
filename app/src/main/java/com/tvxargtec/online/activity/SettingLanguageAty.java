package com.tvxargtec.online.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;

public class SettingLanguageAty extends BaseActivity {

    private ListView listView;
    private final String[][] languages = {
        {"en", "English"},
        {"es", "Español"},
        {"pt", "Português"},
        {"fr", "Français"},
        {"de", "Deutsch"},
        {"it", "Italiano"},
        {"ru", "Русский"},
        {"zh", "中文"}
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_setting_language;
    }

    @Override
    protected void initView() {
        listView = findViewById(R.id.listView);
    }

    @Override
    protected void initData() {
        String[] names = new String[languages.length];
        for (int i = 0; i < languages.length; i++) names[i] = languages[i][1];

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, TransitionLanguageAty.class);
            intent.putExtra("lang", languages[position][0]);
            startActivity(intent);
            finish();
        });
    }
}
