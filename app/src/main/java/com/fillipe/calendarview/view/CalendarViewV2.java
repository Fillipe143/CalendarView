package com.fillipe.calendarview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarViewV2 extends View {
    private int width, height, oldWidth, oldHeight;

    private int headerTextColor = 0xFF000000;
    private int dayTextColor = 0xFF555555;

    private int currentDayTextColor = 0xFFFF0000;
    private int selectDayTextColor = 0xFFEEEEEE;
    private int selectDayBackgroundColor = 0xFFF44336;
    private int rippleEffectColor = 0xAAAAAAAA;

    private float horizontalMargin, verticalMargin;
    private float textSize = 14;

    private int headerHeight, bodyHeight;
    private float centerPosition;

    private final Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private final Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private Calendar selectedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    private final Bitmap[] calendarBuffers = new Bitmap[3];

    private final String[] DAYS_OF_WEEK = { "D", "S", "T", "Q", "Q", "S", "S" };

    public CalendarViewV2(Context context) {
        super(context);
    }

    public CalendarViewV2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarViewV2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CalendarViewV2(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

        this.updateSize();
        if (this.calendarBuffers[0] == null || this.calendarBuffers[1] == null || this.calendarBuffers[2] == null) {
            this.generateAllBuffers();
            return;
        }

        //Paint paint = new Paint();
        //paint.setColor(Color.BLACK);
        //canvas.drawRect(0, 0, this.width / 4.0f, this.headerHeight, paint);
        //canvas.drawRect(3 * this.width /4.0f, 0, this.width , this.headerHeight, paint);

        canvas.drawBitmap(this.getHeaderBitmap(this.currentCalendar), 0, 0, null);
        canvas.drawBitmap(this.calendarBuffers[0], this.centerPosition - 2*this.width, this.headerHeight, null);
        canvas.drawBitmap(this.calendarBuffers[1], this.centerPosition - this.width, this.headerHeight, null);
        canvas.drawBitmap(this.calendarBuffers[2], this.centerPosition, this.headerHeight, null);
    }

    private float lastX, lastY;
    private long lastClickTime = 0;
    private float originalCenterX = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.lastX =  event.getX();
                this.lastY =  event.getY();
                this.originalCenterX = this.centerPosition;
                this.lastClickTime = System.currentTimeMillis();
                return true;

            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - this.lastX;
                if (deltaX >= 50) this.lastClickTime = 0;
                this.centerPosition = Math.max(0, Math.min(this.originalCenterX+ deltaX, this.width * 2));
                this.invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                // Select only if user clicks quickly
                if (System.currentTimeMillis() - this.lastClickTime <= 500) {
                    this.onClicked(this.lastX, this.lastY);
                }

                this.lastX = 0;
                this.lastClickTime = 0;

                // Generate new buffers
                if (centerPosition == this.width*2) this.moveBuffersRight();
                else if (centerPosition <= 50) this.moveBuffersLeft();

                return true;
        }
        return false;
    }

    private void onClicked(float x, float y) {
        if (y > this.headerHeight) {
            // Select date
            y -= this.headerHeight;
            int firstDay = this.getFirstDayOfMonth(this.currentCalendar);
            int amountOfDays = this.getAmountOfDays(this.currentCalendar);
            int lines = (int) Math.ceil((amountOfDays + firstDay) /7.0) - 1;

            int xColumn = (int) (x / (this.width / 7));
            int yColumn = (int)  y / (this.bodyHeight / lines);
            int clickedDay = (7 * yColumn) + xColumn - firstDay + 2;

            if (clickedDay >= 1 && clickedDay <= amountOfDays) {
                Calendar newSelectedDate = (Calendar) this.currentCalendar.clone();
                newSelectedDate.set(Calendar.DAY_OF_MONTH, clickedDay);
                this.setSelectedDate(newSelectedDate);
            }
        } else {
            if (x <= this.width / 4.0f){
                this.moveBuffersRight();
                Toast.makeText(getContext(), this.currentCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), Toast.LENGTH_SHORT).show();
            }
            else if (x >= 3.0f * this.width / 4.0f) {
                this.moveBuffersLeft();
                Toast.makeText(getContext(), this.currentCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateCurrentBuffer() {
        this.calendarBuffers[1] = this.getBodyBitmap(this.currentDate);
        this.invalidate();
    }

    private void generateAllBuffers() {
        Calendar previousCalendar = (Calendar) this.currentCalendar.clone();
        Calendar nextCalendar = (Calendar) this.currentCalendar.clone();

        previousCalendar.add(Calendar.MONTH, -1);
        nextCalendar.add(Calendar.MONTH, 1);

        this.calendarBuffers[0] = this.getBodyBitmap(previousCalendar);
        this.calendarBuffers[1] = this.getBodyBitmap(this.currentCalendar);
        this.calendarBuffers[2] = this.getBodyBitmap(nextCalendar);
        this.invalidate();
    }

    private void moveBuffersLeft() {
        this.currentCalendar.add(Calendar.MONTH, 1);
        this.centerPosition += this.width;
        this.generateAllBuffers();
    }

    private void moveBuffersRight() {
        this.currentCalendar.add(Calendar.MONTH, -1);
        this.centerPosition = this.width;
        this.generateAllBuffers();
    }

    private Bitmap getHeaderBitmap(Calendar calendar) {
        Bitmap bitmap = Bitmap.createBitmap(this.width, this.headerHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(this.headerTextColor);
        paint.setTextSize(this.spToPx(this.textSize));

        // Draw title
        float x = this.horizontalMargin, y = this.verticalMargin;
        canvas.drawText(this.getHeaderTitle(calendar), (float) this.width /2, y, paint);

        // Draw arrows
        canvas.drawText("<", this.width / 8.0f, this.headerHeight / 2.0f, paint);
        canvas.drawText(">", 7.0f * this.width / 8.0f, this.headerHeight / 2.0f, paint);

        //Draw days of week
        paint.setColor(this.dayTextColor);
        y += this.verticalMargin/2 + paint.getTextSize();
        for (String day : DAYS_OF_WEEK) {
            canvas.drawText(day, x, y, paint);
            x += 2 * this.horizontalMargin + paint.measureText(day);
        }

        // Draw divider
        canvas.drawLine(0, this.headerHeight, this.width, this.headerHeight, paint);
        return bitmap;
    }

    private Bitmap getBodyBitmap(Calendar calendar) {
        Bitmap bitmap = Bitmap.createBitmap(this.width, this.bodyHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(this.dayTextColor);
        paint.setTextSize(this.spToPx(this.textSize));

        int amountOfDays = this.getAmountOfDays(calendar);
        int dayOfWeek = this.getFirstDayOfMonth(calendar);

        // Draw days
        float x = this.horizontalMargin + (this.width / 7.0f * (dayOfWeek - 1));
        float y = this.verticalMargin;

        for (int day = 1; day < amountOfDays; day++) {
            calendar.set(Calendar.DAY_OF_MONTH, day);

            paint.setColor(this.dayTextColor);
            if (this.selectedDate != null && this.isSameDay(calendar, this.selectedDate)) {
                paint.setColor(this.selectDayBackgroundColor);
                float ny = y - paint.getTextSize()/2.0f + 4;
                canvas.drawCircle(x, ny, paint.getTextSize(), paint);
                paint.setColor(this.selectDayTextColor);
            } else {
                if (this.isSameDay(calendar, this.currentDate)) paint.setColor(this.currentDayTextColor);
                else paint.setColor(this.dayTextColor);
            }

            canvas.drawText(String.valueOf(day), x, y, paint);
            x += (this.width / 7.0f);
            dayOfWeek++;

            if (dayOfWeek == 8) {
                x = this.horizontalMargin;
                y += this.verticalMargin + paint.getTextSize();
                dayOfWeek = 1;
            }
        }

        return bitmap;
    }

    private String getHeaderTitle(Calendar calendar) {
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        return month + " " + year;
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH)
                && a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
                && a.get(Calendar.YEAR) == b.get(Calendar.YEAR);
    }

    private int getAmountOfDays(Calendar calendar) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1).lengthOfMonth() + 1;
        } else {
            return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
    }

    private int getFirstDayOfMonth(Calendar calendar) {
        Calendar firstDayCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        return firstDayCalendar.get(Calendar.DAY_OF_WEEK);
    }

    private void updateSize() {
        Paint paint = new Paint();
        paint.setTextSize(this.spToPx(this.textSize));
        float textSize = paint.measureText(String.join("", DAYS_OF_WEEK));

        this.horizontalMargin = (this.width - textSize) / 7 / 2;
        this.verticalMargin = 3 * this.horizontalMargin / 2;

        this.headerHeight = (int) (3*this.verticalMargin/2 + 2 * paint.getTextSize());

        int amountOfDays = this.getAmountOfDays(this.selectedDate != null ? this.selectedDate : this.currentDate);
        int firstDay = this.getFirstDayOfMonth(this.selectedDate != null ? this.selectedDate : this.currentDate);
        int lines = (int) Math.ceil((amountOfDays + firstDay) /7.0) - 1;

        this.bodyHeight = (int) ((lines * this.verticalMargin) + (lines * paint.getTextSize()));

        if (this.width != this.oldWidth || this.height != this.oldHeight) {
            this.centerPosition = this.width;
            this.oldWidth = width;
            this.oldHeight = height;
            setLayoutParams(new LinearLayout.LayoutParams(this.width, this.headerHeight + this.bodyHeight));
        }
    }

    private int spToPx(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getContext().getResources().getDisplayMetrics());
    }

    public Calendar getSelectedDate() {
        return this.selectedDate;
    }

    public void setSelectedDate(Calendar selectedDate) {
        this.selectedDate = selectedDate;
        this.generateAllBuffers();
    }

    public int getHeaderTextColor() {
        return this.headerTextColor;
    }

    public void setHeaderTextColor(int headerTextColor) {
        this.headerTextColor = headerTextColor;
    }

    public int getDayTextColor() {
        return this.dayTextColor;
    }

    public void setDayTextColor(int dayTextColor) {
        this.dayTextColor = dayTextColor;
    }

    public int getCurrentDayTextColor() {
        return this.currentDayTextColor;
    }

    public void setCurrentDayTextColor(int currentDayTextColor) {
        this.currentDayTextColor = currentDayTextColor;
    }

    public int getSelectDayBackgroundColor() {
        return this.selectDayBackgroundColor;
    }

    public void setSelectDayBackgroundColor(int selectDayBackgroundColor) {
        this.selectDayBackgroundColor = selectDayBackgroundColor;
    }

    public int getSelectDayTextColor() {
        return this.selectDayTextColor;
    }

    public void setSelectDayTextColor(int selectDayTextColor) {
        this.selectDayTextColor = selectDayTextColor;
    }

    public int getRippleEffectColor() {
        return this.rippleEffectColor;
    }

    public void setRippleEffectColor(int rippleEffectColor) {
        this.rippleEffectColor = rippleEffectColor;
    }

    public float getTextSize() {
        return this.textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }
}