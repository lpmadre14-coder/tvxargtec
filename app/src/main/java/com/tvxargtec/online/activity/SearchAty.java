package com.tvxargtec.online.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class SearchAty extends BaseActivity {

    private EditText etSearch;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> allItems = new ArrayList<>();

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_search;
    }

    @Override
    protected void initView() {
        etSearch = findViewById(R.id.etSearch);
        listView = findViewById(R.id.listView);
        ImageView btnClear = findViewById(R.id.btnClear);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allItems);
        listView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        btnClear.setOnClickListener(v -> etSearch.setText(""));
    }

    @Override
    protected void initData() {
        allItems.add("Canal 1 - Noticias");
        allItems.add("Canal 2 - Deportes");
        allItems.add("Canal 3 - Películas");
        allItems.add("Canal 4 - Series");
        allItems.add("Canal 5 - Infantil");
    }

    private void filter(String query) {
        List<String> filtered = new ArrayList<>();
        for (String item : allItems) {
            if (item.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(item);
            }
        }
        adapter.clear();
        adapter.addAll(filtered);
    }
}
