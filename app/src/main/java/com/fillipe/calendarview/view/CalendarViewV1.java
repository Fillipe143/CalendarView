package com.fillipe.calendarview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** @noinspection unused*/
public class CalendarViewV1 extends View {
    private final Paint paint = new Paint();
    private int width, height;

    private int headerBackgroundColor = 0xFF181818;
    private int headerDayTextColor = 0xFFEEEEEE;

    private int bodyBackgroundColor = 0xFF282828;
    private int enableDaysTextColor = 0xFFEEEEEE;
    private int disableDaysTextColor = 0xFFDDDDDD;

    private int selectDayTextColor = 0xFFEEEEEE;
    private int selectDayBackgroundColor = 0xFFFF0000;

    private String[] daysOfWeek = { "dom", "seg", "ter", "qua", "qui", "sex", "sab" };

    private float textSize = 40.0f;
    private float textVerticalPadding = 24.0f;
    private float headerHeight = this.textSize + 2 * this.textVerticalPadding;

    public CalendarViewV1(Context context) {
        super(context);
        this.init();
    }

    public CalendarViewV1(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public CalendarViewV1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    public CalendarViewV1(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    private  void init() {
        this.paint.setAntiAlias(true);
        this.paint.setTextSize(this.textSize);
        this.paint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        this.width = w;
        this.height = h;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Draw Header
        canvas.drawBitmap(getCalendarHeader(), 0, 0, null);

        // Draw Body
        canvas.drawBitmap(getCalendarBody(), 0, this.headerHeight, null);
    }

    private Bitmap getCalendarHeader() {
        Bitmap headerBitmap = Bitmap.createBitmap(this.width, (int) this.headerHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(headerBitmap);

        // Draw Background
        this.paint.setColor(this.headerBackgroundColor);
        canvas.drawRect(0, 0, this.width, this.headerHeight, this.paint);

        // Draw day of week
        float spacing = (float) this.width / this.daysOfWeek.length;
        this.paint.setColor(this.headerDayTextColor);

        for (int i = 0; i < this.daysOfWeek.length; i++) {
            int x = (int) (spacing * i + spacing/2);
            int y = (int) ((this.headerHeight / 2) - ((this.paint.descent() + this.paint.ascent()) / 2));
            canvas.drawText(this.daysOfWeek[i], x, y, this.paint);
        }

        return headerBitmap;
    }

    private Bitmap getCalendarBody() {
        float bodyHeight = this.height - this.headerHeight;
        Bitmap bodyBitmap = Bitmap.createBitmap(this.width, (int) bodyHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bodyBitmap);

        this.paint.setColor(this.bodyBackgroundColor);
        canvas.drawRect(0, 0, this.width, bodyHeight, this.paint);

        // Draw days
        this.paint.setColor(this.enableDaysTextColor);
        float spacing = (float) this.width / this.daysOfWeek.length;
        //float x = spacing / 2;
        float y =  ((this.headerHeight / 2) - ((this.paint.descent() + this.paint.ascent()) / 2)) + this.textVerticalPadding;
        for (int day = 1; day <= 31; day++) {
            if (day % 8 == 0) y += this.textSize + 2 * this.textVerticalPadding;
            int x = (int) (spacing * ((day%8) - 1) + spacing/2);
            //int y = (int) ((this.headerHeight / 2) - ((this.paint.descent() + this.paint.ascent()) / 2));
            canvas.drawText(String.valueOf(day), x, y, this.paint);
        }
        return bodyBitmap;
    }

    private void updateHeaderHeight() {
        this.headerHeight = this.textSize + 2 * this.textVerticalPadding;
    }

    public int getHeaderBackgroundColor() {
        return headerBackgroundColor;
    }

    public void setHeaderBackgroundColor(int headerBackgroundColor) {
        this.headerBackgroundColor = headerBackgroundColor;
    }

    public int getHeaderDayTextColor() {
        return headerDayTextColor;
    }

    public void setHeaderDayTextColor(int headerDayTextColor) {
        this.headerDayTextColor = headerDayTextColor;
    }

    public int getBodyBackgroundColor() {
        return bodyBackgroundColor;
    }

    public void setBodyBackgroundColor(int bodyBackgroundColor) {
        this.bodyBackgroundColor = bodyBackgroundColor;
    }

    public int getEnableDaysTextColor() {
        return enableDaysTextColor;
    }

    public void setEnableDaysTextColor(int enableDaysTextColor) {
        this.enableDaysTextColor = enableDaysTextColor;
    }

    public int getDisableDaysTextColor() {
        return disableDaysTextColor;
    }

    public void setDisableDaysTextColor(int disableDaysTextColor) {
        this.disableDaysTextColor = disableDaysTextColor;
    }

    public int getSelectDayTextColor() {
        return selectDayTextColor;
    }

    public void setSelectDayTextColor(int selectDayTextColor) {
        this.selectDayTextColor = selectDayTextColor;
    }

    public int getSelectDayBackgroundColor() {
        return selectDayBackgroundColor;
    }

    public void setSelectDayBackgroundColor(int selectDayBackgroundColor) {
        this.selectDayBackgroundColor = selectDayBackgroundColor;
    }

    public String[] getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String[] daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        this.paint.setTextSize(textSize);
        this.updateHeaderHeight();
    }

    public float getTextVerticalPadding() {
        return textVerticalPadding;
    }

    public void setTextVerticalPadding(float textVerticalPadding) {
        this.textVerticalPadding = textVerticalPadding;
        this.updateHeaderHeight();
    }
}