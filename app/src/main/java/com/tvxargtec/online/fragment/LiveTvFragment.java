package com.tvxargtec.online.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import android.view.ViewGroup;

import java.util.Map;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.tvxargtec.online.R;
import com.tvxargtec.online.adapter.ChannelAdapter;
import com.tvxargtec.online.utils.Channel;
import com.tvxargtec.online.utils.ChannelDataManager;

import java.util.List;
import kotlin.Unit;

public class LiveTvFragment extends Fragment {

    private static final Map<String, String> COUNTRY_NAMES = new HashMap<>();
    static {
        COUNTRY_NAMES.put("AR", "Argentina");
        COUNTRY_NAMES.put("US", "USA");
        COUNTRY_NAMES.put("ES", "España");
        COUNTRY_NAMES.put("MX", "México");
        COUNTRY_NAMES.put("CL", "Chile");
        COUNTRY_NAMES.put("PE", "Perú");
        COUNTRY_NAMES.put("CO", "Colombia");
        COUNTRY_NAMES.put("VE", "Venezuela");
        COUNTRY_NAMES.put("BR", "Brasil");
        COUNTRY_NAMES.put("GB", "UK");
        COUNTRY_NAMES.put("FR", "Francia");
        COUNTRY_NAMES.put("DE", "Alemania");
        COUNTRY_NAMES.put("IT", "Italia");
        COUNTRY_NAMES.put("PT", "Portugal");
        COUNTRY_NAMES.put("CA", "Canadá");
        COUNTRY_NAMES.put("AU", "Australia");
        COUNTRY_NAMES.put("RU", "Rusia");
        COUNTRY_NAMES.put("CN", "China");
        COUNTRY_NAMES.put("JP", "Japón");
        COUNTRY_NAMES.put("IN", "India");
        COUNTRY_NAMES.put("TR", "Turquía");
        COUNTRY_NAMES.put("AR", "Argentina");
        COUNTRY_NAMES.put("UA", "Ucrania");
        COUNTRY_NAMES.put("PL", "Polonia");
        COUNTRY_NAMES.put("NL", "Países Bajos");
        COUNTRY_NAMES.put("BE", "Bélgica");
        COUNTRY_NAMES.put("SE", "Suecia");
        COUNTRY_NAMES.put("NO", "Noruega");
        COUNTRY_NAMES.put("DK", "Dinamarca");
        COUNTRY_NAMES.put("FI", "Finlandia");
        COUNTRY_NAMES.put("GR", "Grecia");
        COUNTRY_NAMES.put("IE", "Irlanda");
        COUNTRY_NAMES.put("AT", "Austria");
        COUNTRY_NAMES.put("CH", "Suiza");
        COUNTRY_NAMES.put("KR", "Corea");
        COUNTRY_NAMES.put("TW", "Taiwán");
        COUNTRY_NAMES.put("HK", "Hong Kong");
        COUNTRY_NAMES.put("EG", "Egipto");
        COUNTRY_NAMES.put("IL", "Israel");
        COUNTRY_NAMES.put("SA", "Arabia Saudita");
        COUNTRY_NAMES.put("AE", "Emiratos Árabes");
        COUNTRY_NAMES.put("DO", "República Dominicana");
        COUNTRY_NAMES.put("CR", "Costa Rica");
        COUNTRY_NAMES.put("PA", "Paraguay");
        COUNTRY_NAMES.put("UY", "Uruguay");
        COUNTRY_NAMES.put("EC", "Ecuador");
        COUNTRY_NAMES.put("BO", "Bolivia");
        COUNTRY_NAMES.put("CU", "Cuba");
        COUNTRY_NAMES.put("PR", "Puerto Rico");
    }

    private static String countryCodeToFlag(String code) {
        if (code == null || code.length() != 2) return code;
        int first = Character.toUpperCase(code.charAt(0)) - 'A' + 0x1F1E6;
        int second = Character.toUpperCase(code.charAt(1)) - 'A' + 0x1F1E6;
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }

    private static String getCountryDisplayName(String code) {
        String name = COUNTRY_NAMES.get(code.toUpperCase());
        return name != null ? name : code;
    }

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvChannels;
    private FrameLayout loadingContainer;
    private EditText etSearch;
    private ImageView btnClearSearch;
    private Chip chipAll, chipMovies, chipSeries, chipSports, chipNews, chipEntertainment, chipMusic;
    private Chip chipCountryAll;
    private Chip selectedChip, selectedCountryChip;
    private HorizontalScrollView countryScroll;
    private View countryChipContainer;
    private String selectedCategory = "";
    private String selectedCountry = "";
    private ChannelAdapter adapter;
    private List<Channel> allChannels;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_tv, container, false);
        initViews(view);
        setupListeners();
        loadChannels();
        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvChannels = view.findViewById(R.id.rvChannels);
        loadingContainer = view.findViewById(R.id.loadingContainer);
        countryScroll = view.findViewById(R.id.countryScroll);
        countryChipContainer = view.findViewById(R.id.countryChipContainer);
        etSearch = view.findViewById(R.id.etSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        int spanCount = ChannelDataManager.isTelevision(requireContext()) ? 4 : 2;
        rvChannels.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        chipAll = view.findViewById(R.id.chipAll);
        chipMovies = view.findViewById(R.id.chipMovies);
        chipSeries = view.findViewById(R.id.chipSeries);
        chipSports = view.findViewById(R.id.chipSports);
        chipNews = view.findViewById(R.id.chipNews);
        chipEntertainment = view.findViewById(R.id.chipEntertainment);
        chipMusic = view.findViewById(R.id.chipMusic);

        if (chipAll != null) {
            chipAll.setChecked(true);
            selectedChip = chipAll;
        }

        chipCountryAll = view.findViewById(R.id.chipCountryAll);
        if (chipCountryAll != null) {
            chipCountryAll.setChecked(true);
            selectedCountryChip = chipCountryAll;
        }
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadChannels);

        View.OnClickListener chipListener = v -> {
            Chip chip = (Chip) v;
            if (selectedChip != null) selectedChip.setChecked(false);
            chip.setChecked(true);
            selectedChip = chip;

            if (chip == chipAll) selectedCategory = "";
            else if (chip == chipMovies) selectedCategory = "movies";
            else if (chip == chipSeries) selectedCategory = "series";
            else if (chip == chipSports) selectedCategory = "sports";
            else if (chip == chipNews) selectedCategory = "news";
            else if (chip == chipEntertainment) selectedCategory = "entertainment";
            else if (chip == chipMusic) selectedCategory = "music";

            filterChannels();
        };

        if (chipAll != null) chipAll.setOnClickListener(chipListener);
        if (chipMovies != null) chipMovies.setOnClickListener(chipListener);
        if (chipSeries != null) chipSeries.setOnClickListener(chipListener);
        if (chipSports != null) chipSports.setOnClickListener(chipListener);
        if (chipNews != null) chipNews.setOnClickListener(chipListener);
        if (chipEntertainment != null) chipEntertainment.setOnClickListener(chipListener);
        if (chipMusic != null) chipMusic.setOnClickListener(chipListener);

        if (chipCountryAll != null) {
            chipCountryAll.setOnClickListener(v -> {
                if (selectedCountryChip != null) selectedCountryChip.setChecked(false);
                chipCountryAll.setChecked(true);
                selectedCountryChip = chipCountryAll;
                selectedCountry = "";
                filterChannels();
            });
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                filterChannels();
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            filterChannels();
        });
    }

    private void loadChannels() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.VISIBLE);

        allChannels = ChannelDataManager.getChannels(requireContext(), "");

        if (adapter == null) {
            adapter = new ChannelAdapter(requireContext(), allChannels, channel -> Unit.INSTANCE);
            rvChannels.setAdapter(adapter);
        } else {
            adapter.updateChannels(allChannels);
        }

        if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

        populateCountryChips();
    }

    private void populateCountryChips() {
        List<String> countries = ChannelDataManager.getCountries(requireContext());
        if (countries.isEmpty()) {
            countryScroll.setVisibility(View.GONE);
            return;
        }
        countryScroll.setVisibility(View.VISIBLE);

        if (countryChipContainer instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) countryChipContainer;
            container.removeViews(1, Math.max(0, container.getChildCount() - 1));

            for (String code : countries) {
                Chip chip = new Chip(requireContext());
                String flag = countryCodeToFlag(code);
                String name = getCountryDisplayName(code);
                chip.setText(flag + " " + name);
                chip.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                chip.setChipBackgroundColorResource(R.color.bg_card);
                chip.setTextColor(getResources().getColor(R.color.text_primary));
                chip.setChipCornerRadius(20f);
                chip.setClickable(true);
                chip.setCheckable(true);

                chip.setOnClickListener(v -> {
                    if (selectedCountryChip != null) selectedCountryChip.setChecked(false);
                    chip.setChecked(true);
                    selectedCountryChip = chip;
                    selectedCountry = code;
                    filterChannels();
                });

                container.addView(chip);
            }
        }
    }

    private void filterChannels() {
        List<Channel> filtered = allChannels;

        String query = etSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            filtered = ChannelDataManager.searchChannels(requireContext(), query);
        }

        if (!selectedCategory.isEmpty()) {
            List<Channel> categoryFiltered = new java.util.ArrayList<>();
            for (Channel c : filtered) {
                if (selectedCategory.equalsIgnoreCase(c.getCategoryId())) {
                    categoryFiltered.add(c);
                }
            }
            if (!categoryFiltered.isEmpty()) {
                filtered = categoryFiltered;
            }
        }

        if (!selectedCountry.isEmpty()) {
            filtered = ChannelDataManager.getChannelsByCountry(requireContext(), selectedCountry);
        }

        adapter.updateChannels(filtered);
    }
}
