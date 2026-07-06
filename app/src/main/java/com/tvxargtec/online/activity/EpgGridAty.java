package com.tvxargtec.online.activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.database.entity.EpgProgrammeEntity;
import com.tvxargtec.online.utils.Channel;
import com.tvxargtec.online.utils.ChannelDataManager;
import com.tvxargtec.online.utils.EpgGridHelper;
import com.tvxargtec.online.utils.EpgManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Pair;

public class EpgGridAty extends BaseActivity {

    private ChipGroup dayChipGroup;
    private FrameLayout timelineContainer;
    private EpgTimelineView timelineView;
    private EpgManager epgManager;

    private List<Channel> channels = new ArrayList<>();
    private List<Pair<String, Long>> weekDays;
    private int selectedDayIndex = 0;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_epg_grid;
    }

    @Override
    protected void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar, "Guia EPG", true);

        dayChipGroup = findViewById(R.id.dayChipGroup);
        timelineContainer = findViewById(R.id.timelineContainer);

        timelineView = new EpgTimelineView(this);
        timelineView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        timelineContainer.addView(timelineView);
    }

    @Override
    protected void initData() {
        epgManager = new EpgManager(this);
        weekDays = EpgGridHelper.INSTANCE.getWeekDays();

        channels = ChannelDataManager.getChannels(this);
        if (channels == null) channels = new ArrayList<>();

        setupDayChips();
        loadDayData(0);
    }

    private void setupDayChips() {
        dayChipGroup.removeAllViews();
        int chipBg = ContextCompat.getColor(this, R.color.bg_surface);
        int chipStroke = ContextCompat.getColor(this, R.color.border_glass);
        int textColor = ContextCompat.getColor(this, R.color.text_secondary);

        for (int i = 0; i < weekDays.size(); i++) {
            Pair<String, Long> day = weekDays.get(i);
            Chip chip = new Chip(this);
            chip.setText(day.getFirst());
            chip.setId(View.generateViewId());
            chip.setClickable(true);
            chip.setCheckable(true);
            chip.setTag(i);
            chip.setTextColor(textColor);
            chip.setChipBackgroundColor(ColorStateList.valueOf(chipBg));
            chip.setChipStrokeColor(ColorStateList.valueOf(chipStroke));
            chip.setChipStrokeWidth(1.5f);
            dayChipGroup.addView(chip);
        }

        if (dayChipGroup.getChildCount() > 0) {
            dayChipGroup.check(dayChipGroup.getChildAt(0).getId());
        }

        dayChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    int index = (int) chip.getTag();
                    if (index != selectedDayIndex) {
                        selectedDayIndex = index;
                        loadDayData(index);
                    }
                }
            }
        });
    }

    private void loadDayData(int dayIndex) {
        if (dayIndex < 0 || dayIndex >= weekDays.size()) return;

        long dayStart = weekDays.get(dayIndex).getSecond();
        Map<String, List<EpgProgrammeEntity>> programmeMap = new HashMap<>();

        for (Channel channel : channels) {
            String cid = channel.getId();
            if (cid == null) continue;
            List<EpgProgrammeEntity> progs = EpgGridHelper.INSTANCE.generateMockEpgData(
                    this, cid, dayStart
            );
            programmeMap.put(cid, progs);
            epgManager.cacheProgrammes(progs);
        }

        timelineView.setData(channels, programmeMap, dayStart);
        timelineView.scrollToNow();
    }

    private static class EpgTimelineView extends View {

        private static final float CHANNEL_WIDTH_DP = 130f;
        private static final float ROW_HEIGHT_DP = 60f;
        private static final float HEADER_HEIGHT_DP = 40f;
        private static final float HOUR_WIDTH_DP = 150f;
        private static final int VISIBLE_START = 6;
        private static final int VISIBLE_END = 23;
        private static final float TOUCH_SLOP_DP = 8f;

        private final float density;
        private final float channelWidth;
        private final float rowHeight;
        private final float headerHeight;
        private final float hourWidth;
        private final float touchSlop;
        private final int totalHours;

        private float scrollX = 0;
        private float scrollY = 0;
        private float maxScrollX = 0;
        private float maxScrollY = 0;
        private float downX, downY;

        private List<Channel> channels = new ArrayList<>();
        private Map<String, List<EpgProgrammeEntity>> programmeMap = new HashMap<>();
        private long dayStart;
        private long now = System.currentTimeMillis();

        private final Paint gridPaint;
        private final Paint headerBgPaint;
        private final Paint colBgPaint;
        private final Paint textPaint;
        private final Paint blockPaint;
        private final Paint nowPaint;
        private final Paint currentBorderPaint;
        private final Paint blockTextPaint;
        private final Paint headerTextPaint;
        private final Paint shadowPaint;

        EpgTimelineView(Context context) {
            super(context);
            density = getResources().getDisplayMetrics().density;
            channelWidth = CHANNEL_WIDTH_DP * density;
            rowHeight = ROW_HEIGHT_DP * density;
            headerHeight = HEADER_HEIGHT_DP * density;
            hourWidth = HOUR_WIDTH_DP * density;
            touchSlop = TOUCH_SLOP_DP * density;
            totalHours = VISIBLE_END - VISIBLE_START;

            gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            gridPaint.setColor(0x15FFFFFF);
            gridPaint.setStrokeWidth(1f);

            headerBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            headerBgPaint.setColor(0xFF141D33);

            colBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            colBgPaint.setColor(0xFF0F1626);

            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(0xFF8A99AD);
            textPaint.setTextSize(11f * density);

            blockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            blockPaint.setColor(0x28FFFFFF);

            nowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            nowPaint.setColor(0xFF7C3AED);
            nowPaint.setStrokeWidth(3f * density);

            currentBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            currentBorderPaint.setColor(0xFF7C3AED);
            currentBorderPaint.setStyle(Paint.Style.STROKE);
            currentBorderPaint.setStrokeWidth(2f * density);

            blockTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            blockTextPaint.setColor(0xFFFFFFFF);
            blockTextPaint.setTextSize(10f * density);

            headerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            headerTextPaint.setColor(0xFFFFFFFF);
            headerTextPaint.setTextSize(12f * density);

            shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shadowPaint.setColor(0x22000000);
        }

        void setData(List<Channel> chs, Map<String, List<EpgProgrammeEntity>> map, long dayStartMs) {
            this.channels = chs != null ? chs : new ArrayList<>();
            this.programmeMap = map != null ? map : new HashMap<>();
            this.dayStart = dayStartMs;
            this.now = System.currentTimeMillis();
            updateScrollBounds();
            invalidate();
        }

        void scrollToNow() {
            long visibleStartMs = dayStart + VISIBLE_START * 3600 * 1000L;
            long visibleEndMs = dayStart + VISIBLE_END * 3600 * 1000L;
            if (now < visibleStartMs || now > visibleEndMs) return;
            float fraction = (float) (now - visibleStartMs) / ((visibleEndMs - visibleStartMs));
            float targetScrollX = fraction * totalHours * hourWidth - getWidth() / 2f + channelWidth / 2f;
            scrollX = Math.max(0, Math.min(targetScrollX, maxScrollX));
            invalidate();
        }

        private void updateScrollBounds() {
            float cw = channelWidth + totalHours * hourWidth + hourWidth;
            float ch = headerHeight + channels.size() * rowHeight;
            maxScrollX = Math.max(0, cw - getWidth());
            maxScrollY = Math.max(0, ch - getHeight());
            scrollX = clamp(scrollX, 0, maxScrollX);
            scrollY = clamp(scrollY, 0, maxScrollY);
        }

        private float clamp(float v, float min, float max) {
            return Math.max(min, Math.min(v, max));
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            updateScrollBounds();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(0xFF0B1020);

            now = System.currentTimeMillis();

            long visStartMs = dayStart + VISIBLE_START * 3600 * 1000L;
            float visDurationMs = (float) (totalHours * 3600 * 1000L);

            float totalContentWidth = channelWidth + totalHours * hourWidth;
            float totalContentHeight = headerHeight + channels.size() * rowHeight;

            canvas.save();
            canvas.clipRect(channelWidth, headerHeight, getWidth(), getHeight());
            canvas.translate(-scrollX, -scrollY);

            for (int i = 0; i <= channels.size(); i++) {
                float y = headerHeight + i * rowHeight;
                canvas.drawLine(0, y, totalContentWidth, y, gridPaint);
            }
            for (int h = 0; h <= totalHours; h++) {
                float x = channelWidth + h * hourWidth;
                canvas.drawLine(x, headerHeight, x, totalContentHeight, gridPaint);
            }

            for (int i = 0; i < channels.size(); i++) {
                Channel ch = channels.get(i);
                List<EpgProgrammeEntity> progs = programmeMap.get(ch.getId());
                if (progs == null) continue;
                float rowY = headerHeight + i * rowHeight;
                for (EpgProgrammeEntity p : progs) {
                    long ps = p.getStartTime();
                    long pe = p.getEndTime();
                    long effStart = Math.max(ps, visStartMs);
                    long effEnd = Math.min(pe, visStartMs + (long) visDurationMs);
                    if (effStart >= effEnd) continue;
                    float left = channelWidth + (effStart - visStartMs) / visDurationMs * totalHours * hourWidth;
                    float right = channelWidth + (effEnd - visStartMs) / visDurationMs * totalHours * hourWidth;
                    if (right < channelWidth || left > totalContentWidth) continue;
                    float top = rowY + 4f * density;
                    float bottom = rowY + rowHeight - 4f * density;
                    RectF rect = new RectF(left, top, right, bottom);
                    float radius = 6f * density;
                    boolean isCurrent = ps <= now && pe >= now;
                    if (isCurrent) {
                        canvas.drawRoundRect(rect, radius, radius, blockPaint);
                        canvas.drawRoundRect(rect, radius, radius, currentBorderPaint);
                    } else {
                        canvas.drawRoundRect(rect, radius, radius, blockPaint);
                    }
                    String t = p.getTitle();
                    if (t != null && !t.isEmpty()) {
                        float pw = right - left - 8f * density;
                        String display = t;
                        if (blockTextPaint.measureText(display) > pw && pw > 0) {
                            while (blockTextPaint.measureText(display + "..") > pw && display.length() > 1) {
                                display = display.substring(0, display.length() - 1);
                            }
                            display += "..";
                        }
                        Paint.FontMetrics fm = blockTextPaint.getFontMetrics();
                        float ty = top + (bottom - top) / 2f - (fm.ascent + fm.descent) / 2f;
                        canvas.drawText(display, left + 4f * density, ty, blockTextPaint);
                    }
                }
            }

            if (now >= visStartMs && now <= visStartMs + (long) visDurationMs) {
                float nf = (now - visStartMs) / visDurationMs;
                float nx = channelWidth + nf * totalHours * hourWidth;
                canvas.drawLine(nx, headerHeight, nx, totalContentHeight, nowPaint);
            }

            canvas.restore();

            canvas.save();
            canvas.clipRect(channelWidth, 0, getWidth(), headerHeight);
            canvas.translate(-scrollX, 0);
            canvas.drawRect(channelWidth, 0, totalContentWidth, headerHeight, headerBgPaint);
            for (int h = 0; h <= totalHours; h++) {
                float x = channelWidth + h * hourWidth;
                String label = (VISIBLE_START + h) + ":00";
                canvas.drawText(label, x + 6f * density, headerHeight / 2f + textPaint.getTextSize() / 3f, textPaint);
                canvas.drawLine(x, 0, x, headerHeight, gridPaint);
            }
            Paint hLine = new Paint(Paint.ANTI_ALIAS_FLAG);
            hLine.setColor(0x33FFFFFF);
            hLine.setStrokeWidth(1f);
            canvas.drawLine(channelWidth, headerHeight - 1f, totalContentWidth, headerHeight - 1f, hLine);
            if (now >= visStartMs && now <= visStartMs + (long) visDurationMs) {
                float nf = (now - visStartMs) / visDurationMs;
                float nx = channelWidth + nf * totalHours * hourWidth;
                canvas.drawLine(nx, 0, nx, headerHeight, nowPaint);
            }
            canvas.restore();

            canvas.save();
            canvas.clipRect(0, headerHeight, channelWidth, getHeight());
            canvas.translate(0, -scrollY);
            canvas.drawRect(0, headerHeight, channelWidth, totalContentHeight, colBgPaint);
            Paint colBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            colBorder.setColor(0x33FFFFFF);
            colBorder.setStrokeWidth(1f);
            canvas.drawLine(channelWidth - 1f, headerHeight, channelWidth - 1f, totalContentHeight, colBorder);
            for (int i = 0; i < channels.size(); i++) {
                float ry = headerHeight + i * rowHeight;
                String title = channels.get(i).getTitle();
                if (title == null) title = "";
                float avail = channelWidth - 12f * density;
                String display = title;
                if (headerTextPaint.measureText(display) > avail && avail > 0) {
                    while (headerTextPaint.measureText(display + "..") > avail && display.length() > 1) {
                        display = display.substring(0, display.length() - 1);
                    }
                    display += "..";
                }
                Paint.FontMetrics fm = headerTextPaint.getFontMetrics();
                float ty = ry + rowHeight / 2f - (fm.ascent + fm.descent) / 2f;
                canvas.drawText(display, 6f * density, ty, headerTextPaint);
            }
            canvas.restore();

            canvas.drawRect(0, 0, channelWidth, headerHeight, colBgPaint);
            String cornerLabel = "Canales";
            float cw = headerTextPaint.measureText(cornerLabel);
            Paint.FontMetrics cfm = headerTextPaint.getFontMetrics();
            float cty = headerHeight / 2f - (cfm.ascent + cfm.descent) / 2f;
            canvas.drawText(cornerLabel, (channelWidth - cw) / 2f, cty, headerTextPaint);

            canvas.drawRect(0, headerHeight, getWidth(), headerHeight + 3f * density, shadowPaint);
            canvas.drawRect(channelWidth, 0, channelWidth + 3f * density, getHeight(), shadowPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = x;
                    downY = y;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = downX - x;
                    float dy = downY - y;
                    scrollX = clamp(scrollX + dx, 0, maxScrollX);
                    scrollY = clamp(scrollY + dy, 0, maxScrollY);
                    downX = x;
                    downY = y;
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    float dist = (float) Math.sqrt(Math.pow(x - downX, 2) + Math.pow(y - downY, 2));
                    if (dist < touchSlop) {
                        handleTap(x, y);
                    }
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
            }
            return super.onTouchEvent(e);
        }

        private void handleTap(float vx, float vy) {
            float cx = vx + scrollX;
            float cy = vy + scrollY;
            if (cx < channelWidth || cy < headerHeight) return;
            long visStartMs = dayStart + VISIBLE_START * 3600 * 1000L;
            float visDurationMs = (float) (totalHours * 3600 * 1000L);
            float fraction = (cx - channelWidth) / (totalHours * hourWidth);
            long clickTime = visStartMs + (long) (fraction * visDurationMs);
            int idx = (int) ((cy - headerHeight) / rowHeight);
            if (idx < 0 || idx >= channels.size()) return;
            Channel ch = channels.get(idx);
            List<EpgProgrammeEntity> progs = programmeMap.get(ch.getId());
            if (progs == null) return;
            for (EpgProgrammeEntity p : progs) {
                if (p.getStartTime() <= clickTime && p.getEndTime() >= clickTime) {
                    showProgDialog(p);
                    return;
                }
            }
        }

        private void showProgDialog(EpgProgrammeEntity p) {
            Context ctx = getContext();
            if (ctx == null) return;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String startStr = sdf.format(new Date(p.getStartTime()));
            String endStr = sdf.format(new Date(p.getEndTime()));
            String desc = p.getDescription();
            if (desc == null || desc.isEmpty()) desc = "Sin descripcion.";
            String cat = p.getCategory();
            if (cat == null) cat = "N/A";
            String msg = "Horario: " + startStr + " - " + endStr + "\n\n"
                    + desc + "\n\nCategoria: " + cat;
            new AlertDialog.Builder(ctx)
                    .setTitle(p.getTitle())
                    .setMessage(msg)
                    .setPositiveButton("Cerrar", null)
                    .show();
        }
    }
}
