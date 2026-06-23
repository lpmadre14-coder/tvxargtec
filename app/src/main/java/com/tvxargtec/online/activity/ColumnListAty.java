package com.tvxargtec.online.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class ColumnListAty extends BaseActivity {

    private ListView listView;
    private List<String> categories = new ArrayList<>();

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_column_list;
    }

    @Override
    protected void initView() {
        listView = findViewById(R.id.listView);
    }

    @Override
    protected void initData() {
        String category = getIntent().getStringExtra("category");
        setTitle(category != null ? category : "Categorías");

        categories.add("Noticias");
        categories.add("Deportes");
        categories.add("Películas");
        categories.add("Series");
        categories.add("Infantil");
        categories.add("Música");
        categories.add("Documentales");
        categories.add("Internacional");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, categories);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, SingleColumnAty.class);
            intent.putExtra("category", categories.get(position));
            startActivity(intent);
        });
    }
}
