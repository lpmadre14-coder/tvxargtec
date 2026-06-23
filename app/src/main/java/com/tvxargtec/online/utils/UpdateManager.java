package com.tvxargtec.online.utils;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.core.app.NotificationCompat;

import com.tvxargtec.online.R;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Interceptor;

public class UpdateManager {

    private static final String GITHUB_API = "https://api.github.com/repos/%s/%s/releases/latest";
    private static final String CHANNEL_ID = "update_channel";
    private static final int NOTIFY_ID = 1001;

    private String repoOwner;
    private String repoName;
    private Context context;
    private OkHttpClient client;

    public UpdateManager(Context context, String repoOwner, String repoName) {
        this.context = context;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder()
                                .header("User-Agent", "tvxargtec-android/1.0")
                                .header("Accept", "application/vnd.github.v3+json");
                        return chain.proceed(builder.build());
                    }
                })
                .build();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Actualizaciones",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notificaciones de actualización de la app");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    public void checkForUpdates(UpdateListener listener) {
        String url = String.format(GITHUB_API, repoOwner, repoName);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .post(() -> listener.onError("Error de conexión: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful() || response.body() == null) {
                        if (listener != null) {
                            String msg;
                            switch (response.code()) {
                                case 403: msg = "Límite de API excedido (403). Intenta más tarde."; break;
                                case 404: msg = "No se encontraron versiones en GitHub."; break;
                                case 429: msg = "Demasiadas solicitudes. Espera unos minutos."; break;
                                default: msg = "Error " + response.code();
                            }
                            new android.os.Handler(android.os.Looper.getMainLooper())
                                    .post(() -> listener.onError(msg));
                        }
                        return;
                    }

                    String json = response.body().string();
                    JSONObject release = new JSONObject(json);
                    String tagName = release.optString("tag_name", "");
                    String apkUrl = findApkUrl(release);

                    if (apkUrl == null && listener != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper())
                                .post(() -> listener.onError("No se encontró APK en la release"));
                        return;
                    }

                    int remoteVersion = parseVersionCode(tagName);
                    int currentVersion = getCurrentVersionCode();

                    if (remoteVersion > currentVersion) {
                        String releaseNotes = release.optString("body", "Nueva versión disponible");
                        if (listener != null) {
                            String finalApkUrl = apkUrl;
                            new android.os.Handler(android.os.Looper.getMainLooper())
                                    .post(() -> listener.onUpdateAvailable(tagName, releaseNotes, finalApkUrl));
                        }
                    } else {
                        if (listener != null) {
                            new android.os.Handler(android.os.Looper.getMainLooper())
                                    .post(() -> listener.onUpToDate());
                        }
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper())
                                .post(() -> listener.onError("Error al procesar: " + e.getMessage()));
                    }
                }
            }
        });
    }

    private String findApkUrl(JSONObject release) {
        try {
            var assets = release.getJSONArray("assets");
            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                String name = asset.optString("name", "").toLowerCase();
                if (name.endsWith(".apk")) {
                    return asset.optString("browser_download_url");
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private int parseVersionCode(String tag) {
        try {
            String clean = tag.replaceAll("[^0-9.]", "");
            String[] parts = clean.split("\\.");
            int code = 0;
            for (int i = 0; i < Math.min(3, parts.length); i++) {
                code = code * 100 + Integer.parseInt(parts[i]);
            }
            return code;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getCurrentVersionCode() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) info.getLongVersionCode();
            }
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public void downloadAndInstall(String apkUrl) {
        String fileName = "tvxargtec_update.apk";
        File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir == null) downloadDir = context.getCacheDir();
        File outputFile = new File(downloadDir, fileName);

        if (outputFile.exists()) outputFile.delete();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle("Tvxargtec - Actualizando");
        request.setDescription("Descargando nueva versión...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(outputFile));
        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm == null) {
            Toast.makeText(context, "Error: servicio de descargas no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        long downloadId = dm.enqueue(request);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            installApk(outputFile);
                        }
                    }
                    c.close();
                    ctx.unregisterReceiver(this);
                }
            }
        };

        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void installApk(File apkFile) {
        try {
            Uri apkUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", apkFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Error al instalar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public interface UpdateListener {
        void onUpdateAvailable(String version, String notes, String apkUrl);
        void onUpToDate();
        void onError(String error);
    }
}
