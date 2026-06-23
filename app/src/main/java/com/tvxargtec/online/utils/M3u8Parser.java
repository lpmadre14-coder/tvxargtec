package com.tvxargtec.online.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3u8Parser {

    private static final Pattern EXTINF_PATTERN = Pattern.compile(
        "#EXTINF:-?\\d+\\s+(.*?)\\s*,\\s*(.*)"
    );
    private static final Pattern TVG_NAME = Pattern.compile("tvg-name=\"([^\"]*)\"");
    private static final Pattern TVG_LOGO = Pattern.compile("tvg-logo=\"([^\"]*)\"");
    private static final Pattern TVG_ID = Pattern.compile("tvg-id=\"([^\"]*)\"");
    private static final Pattern TVG_COUNTRY = Pattern.compile("tvg-country=\"([^\"]*)\"");
    private static final Pattern GROUP_TITLE = Pattern.compile("group-title=\"([^\"]*)\"");

    private static final Map<String, String> COUNTRY_CATEGORY_MAP = new HashMap<>();

    static {
        COUNTRY_CATEGORY_MAP.put("usa", "movies");
        COUNTRY_CATEGORY_MAP.put("usa vod", "movies");
        COUNTRY_CATEGORY_MAP.put("spain", "movies");
        COUNTRY_CATEGORY_MAP.put("spain vod", "movies");
        COUNTRY_CATEGORY_MAP.put("argentina", "movies");
        COUNTRY_CATEGORY_MAP.put("mexico", "movies");
        COUNTRY_CATEGORY_MAP.put("chile", "movies");
        COUNTRY_CATEGORY_MAP.put("peru", "movies");
        COUNTRY_CATEGORY_MAP.put("colombia", "movies");
        COUNTRY_CATEGORY_MAP.put("venezuela", "movies");
        COUNTRY_CATEGORY_MAP.put("dominican republic", "movies");
        COUNTRY_CATEGORY_MAP.put("costa rica", "movies");
        COUNTRY_CATEGORY_MAP.put("paraguay", "movies");
        COUNTRY_CATEGORY_MAP.put("equador", "movies");
        COUNTRY_CATEGORY_MAP.put("uk", "series");
        COUNTRY_CATEGORY_MAP.put("canada", "series");
        COUNTRY_CATEGORY_MAP.put("australia", "series");
        COUNTRY_CATEGORY_MAP.put("france", "series");
        COUNTRY_CATEGORY_MAP.put("germany", "series");
        COUNTRY_CATEGORY_MAP.put("italy", "series");
        COUNTRY_CATEGORY_MAP.put("portugal", "series");
        COUNTRY_CATEGORY_MAP.put("brazil", "series");
        COUNTRY_CATEGORY_MAP.put("news", "news");
        COUNTRY_CATEGORY_MAP.put("news (ar)", "news");
        COUNTRY_CATEGORY_MAP.put("news (es)", "news");
        COUNTRY_CATEGORY_MAP.put("weather", "news");
        COUNTRY_CATEGORY_MAP.put("business", "news");
        COUNTRY_CATEGORY_MAP.put("documentaries (en)", "documentaries");
        COUNTRY_CATEGORY_MAP.put("documentaries (ar)", "documentaries");
        COUNTRY_CATEGORY_MAP.put("vod movies (en)", "movies");
        COUNTRY_CATEGORY_MAP.put("vod italy", "series");
    }

    public static List<Channel> parseFromAssets(Context context, String fileName) {
        List<Channel> channels = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            parse(reader, channels);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return channels;
    }

    public static List<Channel> parseLines(List<String> lines) {
        List<Channel> channels = new ArrayList<>();
        parse(lines, channels);
        return channels;
    }

    private static void parse(BufferedReader reader, List<Channel> channels) throws IOException {
        String line;
        String currentExtinf = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#EXTINF:")) {
                currentExtinf = line;
            } else if (!line.startsWith("#") && !line.isEmpty() && currentExtinf != null) {
                Channel ch = parseEntry(currentExtinf, line);
                if (ch != null) {
                    channels.add(ch);
                }
                currentExtinf = null;
            }
        }
    }

    private static void parse(List<String> lines, List<Channel> channels) {
        String currentExtinf = null;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#EXTINF:")) {
                currentExtinf = line;
            } else if (!line.startsWith("#") && !line.isEmpty() && currentExtinf != null) {
                Channel ch = parseEntry(currentExtinf, line);
                if (ch != null) {
                    channels.add(ch);
                }
                currentExtinf = null;
            }
        }
    }

    private static Channel parseEntry(String extinfLine, String url) {
        Matcher m = EXTINF_PATTERN.matcher(extinfLine);
        if (!m.matches()) return null;

        String attrs = m.group(1);
        String title = m.group(2).trim();

        String name = extract(TVG_NAME, attrs, title);
        String logo = extract(TVG_LOGO, attrs, "");
        String tvgId = extract(TVG_ID, attrs, "");
        String country = extract(TVG_COUNTRY, attrs, "");
        String groupTitle = extract(GROUP_TITLE, attrs, "");

        String categoryId = mapCategory(groupTitle.toLowerCase());
        String categoryName = groupTitle.isEmpty() ? "General" : groupTitle;

        return new Channel(tvgId, name, url, logo, categoryId, categoryName, true);
    }

    private static String extract(Pattern pattern, String input, String fallback) {
        Matcher m = pattern.matcher(input);
        if (m.find()) {
            String val = m.group(1).trim();
            return val.isEmpty() ? fallback : val;
        }
        return fallback;
    }

    private static String mapCategory(String groupLower) {
        for (Map.Entry<String, String> entry : COUNTRY_CATEGORY_MAP.entrySet()) {
            if (groupLower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "";
    }
}
