package com.tvxargtec.online.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChannelDataManager {

    private static final String ASSETS_FILE = "iptv_channels.m3u8";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private static List<Channel> cachedChannels = null;
    private static List<String> cachedCountries = null;
    private static final List<String> REMOTE_M3U_SOURCES = new ArrayList<>();
    private static final List<String> CATEGORY_M3U_SOURCES = new ArrayList<>();
    private static final String CUSTOM_M3U_PREF = "custom_m3u_url";

    static {
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/index.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/ar.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/us.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/es.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/mx.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/cl.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/co.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/pe.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/br.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/ve.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/gb.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/fr.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/de.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/it.m3u");
        REMOTE_M3U_SOURCES.add("https://iptv-org.github.io/iptv/countries/pt.m3u");

        CATEGORY_M3U_SOURCES.add("https://iptv-org.github.io/iptv/categories/sports.m3u");
        CATEGORY_M3U_SOURCES.add("https://iptv-org.github.io/iptv/categories/movies.m3u");
        CATEGORY_M3U_SOURCES.add("https://iptv-org.github.io/iptv/categories/news.m3u");
        CATEGORY_M3U_SOURCES.add("https://iptv-org.github.io/iptv/categories/music.m3u");
        CATEGORY_M3U_SOURCES.add("https://iptv-org.github.io/iptv/categories/documentary.m3u");
        CATEGORY_M3U_SOURCES.add("https://iptv-org.github.io/iptv/categories/entertainment.m3u");
        CATEGORY_M3U_SOURCES.add("https://iptv-org.github.io/iptv/categories/kids.m3u");
        CATEGORY_M3U_SOURCES.add("https://iptv-org.github.io/iptv/categories/education.m3u");
        CATEGORY_M3U_SOURCES.add("https://iptv-org.github.io/iptv/categories/anime.m3u");
    }

    public static boolean isTelevision(Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static synchronized List<Channel> getChannels(Context context) {
        if (cachedChannels == null) {
            cachedChannels = loadFromCache(context);
            if (cachedChannels == null || cachedChannels.isEmpty()) {
                cachedChannels = M3u8Parser.parseFromAssets(context, ASSETS_FILE);
            }
            if (cachedChannels == null || cachedChannels.isEmpty()) {
                cachedChannels = getMockChannels("");
            }
        }
        return cachedChannels;
    }

    private static List<Channel> loadFromCache(Context context) {
        try {
            File cacheFile = new File(context.getCacheDir(), "channels_cache.json");
            if (!cacheFile.exists()) return null;
            FileInputStream fis = new FileInputStream(cacheFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            Type type = new TypeToken<List<Channel>>(){}.getType();
            return new Gson().fromJson(sb.toString(), type);
        } catch (Exception e) {
            return null;
        }
    }

    private static void saveToCache(Context context, List<Channel> channels) {
        try {
            File cacheFile = new File(context.getCacheDir(), "channels_cache.json");
            String json = new Gson().toJson(channels);
            FileOutputStream fos = new FileOutputStream(cacheFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(json);
            writer.close();
        } catch (Exception ignored) {}
    }

    public static List<Channel> getChannels(Context context, String category) {
        List<Channel> all = getChannels(context);
        if (category == null || category.isEmpty()) return all;
        List<Channel> filtered = new ArrayList<>();
        for (Channel c : all) {
            if (category.equalsIgnoreCase(c.getCategoryId())) {
                filtered.add(c);
            }
        }
        if (filtered.isEmpty()) {
            List<Channel> fallback = new ArrayList<>(all);
            Collections.shuffle(fallback);
            return fallback.subList(0, Math.min(20, fallback.size()));
        }
        return filtered;
    }

    public static List<Channel> getChannelsByCountry(Context context, String countryCode) {
        List<Channel> all = getChannels(context);
        if (countryCode == null || countryCode.isEmpty()) return all;
        List<Channel> filtered = new ArrayList<>();
        for (Channel c : all) {
            String id = c.getId();
            if (id != null && id.endsWith("." + countryCode.toLowerCase())) {
                filtered.add(c);
            }
        }
        if (!filtered.isEmpty()) return filtered;
        String lower = countryCode.toLowerCase();
        for (Channel c : all) {
            if (c.getCategoryName() != null && c.getCategoryName().toLowerCase().contains(lower)) {
                filtered.add(c);
            }
        }
        return filtered.isEmpty() ? all : filtered;
    }

    public static List<String> getCountries(Context context) {
        if (cachedCountries != null) return cachedCountries;
        List<Channel> all = getChannels(context);
        Set<String> countrySet = new HashSet<>();
        for (Channel c : all) {
            String id = c.getId();
            if (id != null && id.contains(".")) {
                String code = id.substring(id.lastIndexOf(".") + 1);
                if (code.length() == 2) {
                    countrySet.add(code.toUpperCase());
                }
            }
        }
        for (Channel c : all) {
            String name = c.getCategoryName();
            if (name != null) {
                String code = nameToCode(name);
                if (code != null) countrySet.add(code);
            }
        }
        cachedCountries = new ArrayList<>(countrySet);
        Collections.sort(cachedCountries);
        return cachedCountries;
    }

    private static String nameToCode(String name) {
        if (name == null) return null;
        String l = name.toLowerCase();
        if (l.contains("argentina")) return "AR";
        if (l.contains("usa") || l.contains("united states") || l.contains("estados unidos")) return "US";
        if (l.contains("spain") || l.contains("españa") || l.contains("espania")) return "ES";
        if (l.contains("mexico")) return "MX";
        if (l.contains("chile")) return "CL";
        if (l.contains("peru")) return "PE";
        if (l.contains("colombia")) return "CO";
        if (l.contains("venezuela")) return "VE";
        if (l.contains("brazil") || l.contains("brasil")) return "BR";
        if (l.contains("uk") || l.contains("united kingdom")) return "GB";
        if (l.contains("france") || l.contains("francia")) return "FR";
        if (l.contains("germany") || l.contains("alemania")) return "DE";
        if (l.contains("italy") || l.contains("italia")) return "IT";
        if (l.contains("portugal")) return "PT";
        if (l.contains("canada")) return "CA";
        if (l.contains("australia")) return "AU";
        if (l.contains("russia") || l.contains("rusia")) return "RU";
        if (l.contains("china")) return "CN";
        if (l.contains("japan") || l.contains("japon")) return "JP";
        if (l.contains("india")) return "IN";
        if (l.contains("turkey") || l.contains("turquia")) return "TR";
        return null;
    }

    public static List<Channel> searchChannels(Context context, String query) {
        List<Channel> all = getChannels(context);
        if (TextUtils.isEmpty(query)) return all;
        String lower = query.toLowerCase().trim();
        List<Channel> results = new ArrayList<>();
        for (Channel c : all) {
            if (c.getTitle() != null && c.getTitle().toLowerCase().contains(lower)) {
                results.add(c);
            }
        }
        return results;
    }

    public static void fetchRemoteM3USources(Context context, DataCallback callback) {
        loadCustomM3USource(context);
        List<Channel> merged = new ArrayList<>(getChannels(context));
        final int[] completed = {0};
        final int total = REMOTE_M3U_SOURCES.size() + CATEGORY_M3U_SOURCES.size();

        if (total == 0) {
            callback.onDataLoaded(merged);
            return;
        }

        for (String url : REMOTE_M3U_SOURCES) {
            fetchSingleM3U(url, new DataCallback() {
                @Override
                public void onDataLoaded(List<Channel> channels) {
                    synchronized (merged) {
                        merged.addAll(channels);
                        cachedChannels = merged;
                    }
                    completed[0]++;
                    if (completed[0] >= total) {
                        saveToCache(context, merged);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onDataLoaded(merged));
                    }
                }

                @Override
                public void onError(Exception e) {
                    completed[0]++;
                    if (completed[0] >= total) {
                        saveToCache(context, merged);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onDataLoaded(merged));
                    }
                }
            });
        }

        for (String url : CATEGORY_M3U_SOURCES) {
            String category = inferCategoryFromUrl(url);
            fetchSingleM3U(url, new DataCallback() {
                @Override
                public void onDataLoaded(List<Channel> channels) {
                    synchronized (merged) {
                        for (Channel c : channels) {
                            Channel mapped = new Channel(
                                c.getId(), c.getTitle(), c.getUrl(), c.getLogo(),
                                category, c.getCategoryName(), c.isLive()
                            );
                            merged.add(mapped);
                        }
                        cachedChannels = merged;
                    }
                    completed[0]++;
                    if (completed[0] >= total) {
                        saveToCache(context, merged);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onDataLoaded(merged));
                    }
                }

                @Override
                public void onError(Exception e) {
                    completed[0]++;
                    if (completed[0] >= total) {
                        saveToCache(context, merged);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onDataLoaded(merged));
                    }
                }
            });
        }
    }

    private static String inferCategoryFromUrl(String url) {
        if (url.contains("sports")) return "sports";
        if (url.contains("movies")) return "movies";
        if (url.contains("news")) return "news";
        if (url.contains("music")) return "music";
        if (url.contains("documentary")) return "documentaries";
        if (url.contains("entertainment")) return "entertainment";
        if (url.contains("kids")) return "kids";
        if (url.contains("education")) return "education";
        if (url.contains("anime")) return "anime";
        return "";
    }

    private static void fetchSingleM3U(String url, DataCallback callback) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        String[] lines = body.split("\n");
                        List<String> lineList = new ArrayList<>();
                        Collections.addAll(lineList, lines);
                        List<Channel> channels = M3u8Parser.parseLines(lineList);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onDataLoaded(channels));
                    } else {
                        callback.onError(new Exception("HTTP " + response.code()));
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }

    public static List<Channel> getMockChannels(String category) {
        List<Channel> channels = new ArrayList<>();
        channels.add(new Channel("1", "Cine Español", "http://example.com/stream1.m3u8", "@drawable/ic_play", "movies", "Películas", true));
        channels.add(new Channel("2", "Series TV", "http://example.com/stream2.m3u8", "@drawable/ic_play", "series", "Series", true));
        channels.add(new Channel("3", "Deportes 24/7", "http://example.com/stream3.m3u8", "@drawable/ic_play", "sports", "Deportes", true));
        channels.add(new Channel("4", "Noticias Mundo", "http://example.com/stream4.m3u8", "@drawable/ic_play", "news", "Noticias", true));
        channels.add(new Channel("5", "Documentales HD", "http://example.com/stream5.m3u8", "@drawable/ic_play", "documentaries", "Documentales", true));
        channels.add(new Channel("6", "Infantil Kids", "http://example.com/stream6.m3u8", "@drawable/ic_play", "kids", "Infantil", true));
        channels.add(new Channel("7", "Música Top", "http://example.com/stream7.m3u8", "@drawable/ic_play", "music", "Música", true));
        channels.add(new Channel("8", "Cine Clásico", "http://example.com/stream8.m3u8", "@drawable/ic_play", "movies", "Películas", true));
        channels.add(new Channel("9", "Series Clásicas", "http://example.com/stream9.m3u8", "@drawable/ic_play", "series", "Series", true));
        channels.add(new Channel("10", "Fútbol Live", "http://example.com/stream10.m3u8", "@drawable/ic_play", "sports", "Deportes", true));
        if (!category.isEmpty()) {
            List<Channel> filtered = new ArrayList<>();
            for (Channel c : channels) {
                if (c.getCategoryId().equalsIgnoreCase(category)) {
                    filtered.add(c);
                }
            }
            return filtered;
        }
        return channels;
    }

    public static void addCustomM3USource(String url) {
        if (url != null && !url.isEmpty() && !REMOTE_M3U_SOURCES.contains(url)) {
            REMOTE_M3U_SOURCES.add(url);
            cachedChannels = null;
        }
    }

    public static void clearCustomM3USource() {
        // Remove all custom URLs (those not in the default list)
        List<String> defaults = new ArrayList<>();
        defaults.add("https://iptv-org.github.io/iptv/index.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/ar.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/us.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/es.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/mx.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/cl.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/co.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/pe.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/br.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/ve.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/gb.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/fr.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/de.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/it.m3u");
        defaults.add("https://iptv-org.github.io/iptv/countries/pt.m3u");

        REMOTE_M3U_SOURCES.clear();
        REMOTE_M3U_SOURCES.addAll(defaults);
        cachedChannels = null;
    }

    public static void loadCustomM3USource(Context context) {
        String url = context.getSharedPreferences("playlist_prefs", 0).getString(CUSTOM_M3U_PREF, "");
        if (!url.isEmpty() && !REMOTE_M3U_SOURCES.contains(url)) {
            REMOTE_M3U_SOURCES.add(url);
        }
    }

    public interface DataCallback {
        void onDataLoaded(List<Channel> channels);
        void onError(Exception e);
    }
}
