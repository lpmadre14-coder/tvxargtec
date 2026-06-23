package com.tvxargtec.online.activity;

import android.app.PictureInPictureParams;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.ExoTrackSelection;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.cast.CastManager;
import com.tvxargtec.online.utils.EpgHelper;
import com.tvxargtec.online.utils.EpgProgramme;
import com.tvxargtec.online.utils.OfflineManager;

import java.util.ArrayList;
import java.util.List;

public class PlayAty extends BaseActivity {

    private PlayerView playerView;
    private WebView webViewPlayer;
    private ExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private ProgressBar loadingBar;
    private TextView tvError, tvCurrentTime, tvTotalTime;
    private View errorContainer, btnRetry;
    private FrameLayout controlsOverlay;
    private View doubleTapLeft, doubleTapRight;
    private boolean controlsVisible = true;
    private boolean isLocked = false;
    private final Handler controlsHandler = new Handler(Looper.getMainLooper());

    private String videoUrl;
    private String videoTitle;
    private boolean isYouTube = false;
    private CastManager castManager;

    // EPG
    private View epgContainer;
    private TextView tvEpgCurrent, tvEpgNext;

    // PiP
    private boolean isInPipMode = false;

    // Gesture
    private GestureDetector gestureDetector;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_play;
    }

    @Override
    protected void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playerView = findViewById(R.id.playerView);
        webViewPlayer = findViewById(R.id.webViewPlayer);
        loadingBar = findViewById(R.id.loadingBar);
        tvError = findViewById(R.id.tvError);
        errorContainer = findViewById(R.id.errorContainer);
        btnRetry = findViewById(R.id.btnRetry);
        epgContainer = findViewById(R.id.epgContainer);
        tvEpgCurrent = findViewById(R.id.tvEpgCurrent);
        tvEpgNext = findViewById(R.id.tvEpgNext);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        controlsOverlay = findViewById(R.id.controlsOverlay);
        doubleTapLeft = findViewById(R.id.doubleTapLeft);
        doubleTapRight = findViewById(R.id.doubleTapRight);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnLock = findViewById(R.id.btnLock);
        ImageView btnPip = findViewById(R.id.btnPip);
        ImageView btnCast = findViewById(R.id.btnCast);
        ImageView btnDownload = findViewById(R.id.btnDownload);
        ImageView btnSettings = findViewById(R.id.btnSettings);

        btnBack.setOnClickListener(v -> finish());
        btnLock.setOnClickListener(v -> toggleLock());

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> {
                errorContainer.setVisibility(View.GONE);
                btnRetry.setVisibility(View.GONE);
                initializePlayer();
            });
        }
        btnPip.setOnClickListener(v -> enterPip());
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> {
                if (videoUrl != null && !videoUrl.isEmpty()) {
                    String contentId = "download_" + System.currentTimeMillis();
                    OfflineManager.Companion.getInstance(this).downloadContent(contentId,
                            videoTitle != null ? videoTitle : "Contenido",
                            videoUrl,
                            new OfflineManager.DownloadCallback() {
                                @Override
                                public void onProgress(String id, float percent) {
                                    runOnUiThread(() -> showToast("Descargando: " + (int)(percent * 100) + "%"));
                                }

                                @Override
                                public void onCompleted(String id) {
                                    runOnUiThread(() -> showToast("Descarga completa"));
                                }

                                @Override
                                public void onFailed(String id, String error) {
                                    runOnUiThread(() -> showToast("Error: " + error));
                                }
                            });
                    showToast("Iniciando descarga...");
                }
            });
        }

        if (btnCast != null) {
            btnCast.setOnClickListener(v -> {
                if (castManager != null && videoUrl != null && !videoUrl.isEmpty()) {
                    castManager.castVideo(videoUrl, videoTitle != null ? videoTitle : "Tvxargtec", "", "");
                } else {
                    showToast("No hay contenido para transmitir");
                }
            });
        }

        // Player visibility toggle
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            controlsOverlay.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
            if (visibility == View.VISIBLE) {
                controlsHandler.removeCallbacksAndMessages(null);
                controlsHandler.postDelayed(() -> {
                    if (!isLocked) {
                        playerView.hideController();
                        controlsOverlay.setVisibility(View.GONE);
                    }
                }, 4000);
            }
        });

        playerView.setOnClickListener(v -> toggleControls());
        setupGestures();
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SEEK_MS = 10000;

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (player == null || isLocked) return false;
                float width = playerView.getWidth();
                if (e.getX() < width / 3) {
                    seekRelative(-SEEK_MS);
                    showDoubleTapIndicator(doubleTapLeft, -SEEK_MS);
                } else if (e.getX() > width * 2 / 3) {
                    seekRelative(SEEK_MS);
                    showDoubleTapIndicator(doubleTapRight, SEEK_MS);
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleControls();
                return true;
            }
        });

        playerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void seekRelative(int ms) {
        long newPos = player.getCurrentPosition() + ms;
        player.seekTo(Math.max(0, Math.min(newPos, player.getDuration())));
    }

    private void showDoubleTapIndicator(View indicator, int ms) {
        if (indicator == null) return;
        int id = (indicator.getId() == R.id.doubleTapLeft) ? R.id.tvSeekLeft : R.id.tvSeekRight;
        TextView tv = indicator.findViewById(id);
        if (tv != null) tv.setText((ms > 0 ? "+" : "") + (ms / 1000) + "s");
        indicator.setVisibility(View.VISIBLE);
        indicator.setAlpha(1f);
        indicator.animate().alpha(0f).setDuration(800).withEndAction(() ->
                indicator.setVisibility(View.GONE)).start();
    }

    private void toggleControls() {
        if (isLocked) return;
        if (controlsVisible) {
            playerView.hideController();
            controlsOverlay.setVisibility(View.GONE);
        } else {
            playerView.showController();
            controlsOverlay.setVisibility(View.VISIBLE);
            controlsHandler.removeCallbacksAndMessages(null);
            controlsHandler.postDelayed(() -> {
                if (!isLocked) {
                    playerView.hideController();
                    controlsOverlay.setVisibility(View.GONE);
                }
            }, 4000);
        }
        controlsVisible = !controlsVisible;
    }

    private void toggleLock() {
        isLocked = !isLocked;
        showToast(isLocked ? "Controles bloqueados" : "Controles desbloqueados");
        if (isLocked) {
            playerView.hideController();
            controlsOverlay.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initData() {
        videoUrl = getIntent().getStringExtra("url");
        videoTitle = getIntent().getStringExtra("title");
        if (videoUrl == null) videoUrl = "";
        setTitle(videoTitle != null ? videoTitle : "Reproduciendo");

        isYouTube = videoUrl.contains("youtube.com/") || videoUrl.contains("youtu.be/");

        castManager = new CastManager(this);
        castManager.initialize();

        if (isYouTube) {
            setupWebView();
        } else {
            initializePlayer();
        }

        if (videoTitle != null && !videoTitle.isEmpty()) {
            fetchEpg(videoTitle);
        }
    }

    private void fetchEpg(String channelName) {
        if (channelName == null || channelName.isEmpty()) return;
        EpgHelper.fetchNowPlaying(channelName, new EpgHelper.Callback() {
            @Override
            public void invoke(EpgProgramme current, EpgProgramme next) {
                runOnUiThread(() -> {
                    if (current != null && current.getTitle() != null && !current.getTitle().isEmpty()) {
                        epgContainer.setVisibility(View.VISIBLE);
                        tvEpgCurrent.setText("▶ " + current.getTitle());
                        tvEpgNext.setText("Siguiente: " + (next != null ? next.getTitle() : "—"));
                    } else {
                        epgContainer.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void setupWebView() {
        playerView.setVisibility(View.GONE);
        webViewPlayer.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.VISIBLE);

        WebSettings settings = webViewPlayer.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36");

        webViewPlayer.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                loadingBar.setVisibility(View.GONE);
            }
        });

        webViewPlayer.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 80) loadingBar.setVisibility(View.GONE);
            }
        });

        webViewPlayer.loadUrl(videoUrl);
    }

    private String extractYouTubeId(String url) {
        if (url.contains("v=")) {
            String q = url.substring(url.indexOf("v=") + 2);
            int amp = q.indexOf("&");
            return amp > 0 ? q.substring(0, amp) : q;
        }
        if (url.contains("youtu.be/")) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        if (url.contains("/live")) {
            String p = url.substring(url.indexOf("/live") + 6);
            if (p.startsWith("/")) p = p.substring(1);
            int q = p.indexOf("?");
            return q > 0 ? p.substring(0, q) : p;
        }
        return "";
    }

    private void initializePlayer() {
        trackSelector = new DefaultTrackSelector(this);
        DefaultTrackSelector.Parameters.Builder paramsBuilder = trackSelector.getParameters().buildUpon();

        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .build();

        playerView.setPlayer(player);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playerView.setKeepScreenOn(true);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                loadingBar.setVisibility(playbackState == Player.STATE_BUFFERING ? View.VISIBLE : View.GONE);
                if (playbackState == Player.STATE_READY) {
                    errorContainer.setVisibility(View.GONE);
                    if (btnRetry != null) btnRetry.setVisibility(View.GONE);
                    updateTimeInfo();
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                loadingBar.setVisibility(View.GONE);
                String msg = "Error de reproducción";
                String detail = error.getMessage();
                if (detail != null) {
                    if (detail.contains("503") || detail.contains("Response code: 503")) {
                        msg = "Servicio no disponible (503)\nEl canal puede estar fuera de línea o bloqueado en tu región.";
                    } else if (detail.contains("403") || detail.contains("Response code: 403")) {
                        msg = "Acceso denegado (403)\nEste canal requiere autorización.";
                    } else if (detail.contains("404") || detail.contains("Response code: 404")) {
                        msg = "Canal no encontrado (404)";
                    } else if (detail.contains("Unable to resolve host") || detail.contains("Failed to connect")) {
                        msg = "Error de conexión\nVerifica tu conexión a internet.";
                    } else if (detail.contains("java.net.SocketTimeoutException") || detail.contains("timeout")) {
                        msg = "Tiempo de espera agotado\nEl servidor no responde.";
                    } else if (detail.contains("SSLHandshakeException")) {
                        msg = "Error de seguridad SSL";
                    }
                }
                tvError.setText(msg);
                errorContainer.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTracksChanged(@NonNull Tracks tracks) {
                updateTimeInfo();
            }
        });

        player.prepare();
        player.setPlayWhenReady(true);
        showControlsTemporarily();
    }

    private void showControlsTemporarily() {
        playerView.showController();
        controlsOverlay.setVisibility(View.VISIBLE);
        controlsHandler.postDelayed(() -> {
            playerView.hideController();
            controlsOverlay.setVisibility(View.GONE);
            controlsVisible = false;
        }, 3000);
    }

    private void updateTimeInfo() {
        if (player == null) return;
        long duration = player.getDuration();
        long position = player.getCurrentPosition();
        if (tvCurrentTime != null) tvCurrentTime.setText(formatTime(position));
        if (tvTotalTime != null) tvTotalTime.setText(formatTime(duration));
    }

    private String formatTime(long ms) {
        long totalSec = ms / 1000;
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%02d:%02d", m, s);
    }

    // ===== SETTINGS DIALOG =====

    private void showSettingsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_player_settings, null);
        dialog.setContentView(view);

        LinearLayout qualityList = view.findViewById(R.id.qualityList);
        LinearLayout subtitleList = view.findViewById(R.id.subtitleList);
        LinearLayout audioList = view.findViewById(R.id.audioList);

        // Quality
        qualityList.setOnClickListener(v -> {
            showTrackSelector("Calidad", C.TRACK_TYPE_VIDEO);
            dialog.dismiss();
        });

        // Subtitles
        subtitleList.setOnClickListener(v -> {
            showTrackSelector("Subtítulos", C.TRACK_TYPE_TEXT);
            dialog.dismiss();
        });

        // Audio
        audioList.setOnClickListener(v -> {
            showTrackSelector("Audio", C.TRACK_TYPE_AUDIO);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTrackSelector(String title, @C.TrackType int trackType) {
        if (player == null) return;
        Tracks tracks = player.getCurrentTracks();
        List<String> trackNames = new ArrayList<>();
        List<Integer> trackIndices = new ArrayList<>();

        for (int groupIndex = 0; groupIndex < tracks.getGroups().size(); groupIndex++) {
            Tracks.Group group = tracks.getGroups().get(groupIndex);
            if (group.getType() == trackType) {
                for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                    Format format = group.getTrackFormat(trackIndex);
                    String label = buildTrackLabel(format, trackIndex);
                    trackNames.add(label);
                    trackIndices.add(groupIndex);
                }
            }
        }

        if (trackNames.isEmpty()) {
            showToast("No hay opciones disponibles");
            return;
        }

        String[] items = trackNames.toArray(new String[0]);
        int currentTrack = -1;

        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(items, (dialog, which) -> {
                    selectTrack(trackType, trackIndices.get(which), which);
                })
                .show();
    }

    private String buildTrackLabel(Format format, int index) {
        if (format.height > 0) {
            String label = format.height + "p";
            if (format.frameRate > 0) label += " " + (int) format.frameRate + "fps";
            if (format.bitrate > 0) label += " (" + (format.bitrate / 1000000) + " Mbps)";
            return label;
        }
        if (format.language != null) {
            String lang = new java.util.Locale(format.language).getDisplayLanguage();
            if (format.label != null) return lang + " - " + format.label;
            return lang;
        }
        if (format.label != null) return format.label;
        return "Pista " + (index + 1);
    }

    private void selectTrack(@C.TrackType int trackType, int groupIndex, int trackIndex) {
        if (trackSelector == null) return;
        DefaultTrackSelector.Parameters.Builder params = trackSelector.getParameters().buildUpon();
        params.setTrackTypeDisabled(C.TRACK_TYPE_UNKNOWN, true);
        Tracks tracks = player.getCurrentTracks();
        if (groupIndex >= 0 && groupIndex < tracks.getGroups().size()) {
            TrackGroup trackGroup = tracks.getGroups().get(groupIndex).getMediaTrackGroup();
            params.addOverride(new TrackSelectionOverride(trackGroup, trackIndex));
        }
        trackSelector.setParameters(params.build());
    }

    // ===== PIP =====

    private void enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
            pipBuilder.setAspectRatio(new Rational(16, 9));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pipBuilder.setSeamlessResizeEnabled(true);
            }
            enterPictureInPictureMode(pipBuilder.build());
        } else {
            showToast("PiP no disponible en este dispositivo");
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        isInPipMode = isInPictureInPictureMode;
        if (isInPipMode) {
            controlsOverlay.setVisibility(View.GONE);
            playerView.hideController();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && player != null && player.isPlaying()) {
            enterPip();
        }
    }

    // ===== LIFECYCLE =====

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && !isInPipMode) player.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null && !isInPipMode) player.play();
        controlsHandler.postDelayed(this::updateTimeInfo, 500);
    }
    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        // Detecta pulsaciones de control remoto de TV
        if (playerView != null && !isLocked) {
            playerView.showController();
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (isYouTube && webViewPlayer != null && webViewPlayer.canGoBack()) {
            webViewPlayer.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controlsHandler.removeCallbacksAndMessages(null);
        if (castManager != null) castManager.destroy();
        if (player != null) {
            player.release();
            player = null;
        }
        if (webViewPlayer != null) {
            webViewPlayer.removeAllViews();
            webViewPlayer.destroy();
        }
    }
}
