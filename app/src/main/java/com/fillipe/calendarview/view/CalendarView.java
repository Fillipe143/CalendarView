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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarView extends View {

    private int titleTextColor = 0xFF000000;
    private int dayTextColor = 0xFF555555;
    private int todayTextColor = 0xFFFF0000;
    private int selectedDayTextColor = 0xFFEEEEEE;
    private int selectedDayBackgroundColor = 0xFFF44336;
    private int disabledDayTextColor = 0xFFAAAAAA;

    private float textSize = 14; // sp
    private boolean disableAllDates = false;
    private OnDateSelectListener onDateSelectListener = null;

    private Calendar todaysCalendar;
    private Calendar currentMonthCalendar;
    private Calendar selectedDayCalendar;

    private int width, height, oldWidth, oldHeight;
    private int headerHeight, bodyHeight;
    private int horizontalMargin, verticalMargin;

    private Context context;

    private Paint paint;
    private Bitmap headerBitmap, bodyBitmap;

    public CalendarView(Context context) {
        super(context);
        this.init();
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    private void init() {
        this.todaysCalendar = Calendar.getInstance(TimeZone.getDefault());
        this.currentMonthCalendar = Calendar.getInstance(TimeZone.getDefault());
        this.selectedDayCalendar = Calendar.getInstance(TimeZone.getDefault());

        this.context = this.getContext();
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        this.paint.setTextAlign(Paint.Align.CENTER);
        this.paint.setTextSize(Utils.spToPx(this.context, this.textSize));
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

        if (this.headerBitmap == null || this.bodyBitmap == null) {
            this.updateSizes();
            this.updateHeaderBitmap();
            this.updateBodyBitmap();
            return;
        }

        // Render header and body of calendar
        canvas.drawBitmap(this.headerBitmap, 0, 0, null);
        canvas.drawBitmap(this.bodyBitmap, 0, this.headerBitmap.getHeight(), null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (this.headerBitmap == null || this.bodyBitmap == null) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                TouchEventHelper.onActionDown(event);
                return true;

            case MotionEvent.ACTION_MOVE:
                TouchEventHelper.onActionMove(event);
                return true;

            case MotionEvent.ACTION_UP:
                TouchEventHelper.onActionUp();

                if (TouchEventHelper.hadClick) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    if (event.getY() <= this.headerHeight) this.onHeaderClicked(x, y);
                    else this.onBodyClicked(x, y - this.headerHeight);
                }
                return true;
        }

        return false;
    }

    private void updateHeaderBitmap() {
        if (this.width <= 0 || this.headerHeight <= 0) return;
        this.headerBitmap = Bitmap.createBitmap(this.width, this.headerHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(this.headerBitmap);

        int y = this.verticalMargin;

        // Render title
        this.paint.setColor(this.titleTextColor);
        canvas.drawText(Utils.getCalendarTitle(this.currentMonthCalendar), this.width / 2.0f, y, this.paint);

        // Draw Arrows
        canvas.drawText("<", this.width / 8.0f, y, paint);
        canvas.drawText(">", this.width * 7.0f / 8.0f, y, paint);

        int x = this.horizontalMargin;
        y += (int) this.paint.getTextSize();
        y += this.verticalMargin;

        // Draw days of week
        this.paint.setColor(this.dayTextColor);
        for (int day = 1; day <= 7; day++) {
            String dayName = Utils.getDayOfWeekName(day);
            canvas.drawText(dayName, x, y, paint);
            x += (int) (this.horizontalMargin * 2 + paint.measureText(dayName));
        }

        y += this.verticalMargin/2;

        //Draw divider
        canvas.drawLine(0, y, this.width, y, paint);
    }

    private void updateBodyBitmap() {
        if (this.width <= 0 || this.bodyHeight <= 0) return;
        this.bodyBitmap = Bitmap.createBitmap(this.width, this.bodyHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(this.bodyBitmap);

        int amountOfDaysInMonth = Utils.getAmountOfDaysInMonth(this.currentMonthCalendar);
        int currentDayOfWeek = Utils.getFirstDayOfMonth(this.currentMonthCalendar);

        int x = this.horizontalMargin + this.width / 7 * (currentDayOfWeek - 1);
        int y = this.verticalMargin;

        // Render days
        for (int day = 1; day < amountOfDaysInMonth; day++) {
            this.renderDay(canvas, x, y, day);
            currentDayOfWeek = (currentDayOfWeek + 1) % 8;

            x += this.width / 7;
            if (currentDayOfWeek == 0) {
                currentDayOfWeek = 1;
                x = this.horizontalMargin;
                y += (int) (this.verticalMargin + this.paint.getTextSize());
            }
        }
    }

    private void renderDay(Canvas canvas, int x, int y, int day) {
        Calendar dayCalendar = (Calendar) this.currentMonthCalendar.clone();
        dayCalendar.set(Calendar.DAY_OF_MONTH, day);

        if (Utils.isSameDay(dayCalendar, this.selectedDayCalendar)) {
            // Render selected background
            this.paint.setColor(this.selectedDayBackgroundColor);
            canvas.drawCircle(x, y - this.paint.getTextSize() / 3, this.paint.getTextSize(), this.paint);

            // Set text color
            this.paint.setColor(this.selectedDayTextColor);
        } else {
            // Set text color
            if (Utils.isSameDay(dayCalendar, this.todaysCalendar)) this.paint.setColor(this.todayTextColor);
            else if (this.disableAllDates) this.paint.setColor(this.disabledDayTextColor);
            else this.paint.setColor(this.dayTextColor);
        }

        canvas.drawText(String.valueOf(day), x, y, this.paint);
    }

    private void onHeaderClicked(int x, int y) {
        int border = this.width / 4;

        // Forward or back months
        if (x <= border) this.moveRight();
        else if (x >= this.width - border) this.moveLeft();
    }

    private void onBodyClicked(int x, int y) {
        int amountOfDaysInMonth = Utils.getAmountOfDaysInMonth(this.currentMonthCalendar);
        int starterDayOfWeek = Utils.getFirstDayOfMonth(this.currentMonthCalendar);
        int numberOfRows = (int) Math.ceil((amountOfDaysInMonth+ starterDayOfWeek) / 7.0) - 1;

        int column = x  / (this.width / 7) + 2;
        int row = y / (this.bodyHeight / numberOfRows);
        int clickedDay = (row * 7) + column - starterDayOfWeek;

        if (clickedDay >= 1 && clickedDay <= amountOfDaysInMonth) {
            Calendar newSelectedDate = (Calendar) this.currentMonthCalendar.clone();
            newSelectedDate.set(Calendar.DAY_OF_MONTH, clickedDay);
            this.selectDay(newSelectedDate);
        }
    }

    private void updateSizes() {
        // Update margins
        float charWidth = paint.measureText("A");
        this.horizontalMargin = (int) ((this.width - charWidth * 7.0f) / 14.0f);
        this.verticalMargin = this.horizontalMargin * 3 / 2;

        // Update header height
        this.headerHeight = 0;
        this.headerHeight += this.verticalMargin; // Margin
        this.headerHeight += (int) this.paint.getTextSize(); // Title
        this.headerHeight += this.verticalMargin; // Margin
        this.headerHeight += (int) this.paint.getTextSize(); // Days of week
        this.headerHeight += this.verticalMargin/2; // Margin

        // Update body height
        int amountOfDaysInMonth = Utils.getAmountOfDaysInMonth(this.currentMonthCalendar);
        int starterDayOfWeek = Utils.getFirstDayOfMonth(this.currentMonthCalendar);
        int numberOfRows = (int) Math.ceil((amountOfDaysInMonth+ starterDayOfWeek) / 7.0) - 1;

        this.bodyHeight = 0;
        this.bodyHeight += this.verticalMargin;
        this.bodyHeight += this.verticalMargin * numberOfRows;
        this.bodyHeight += (int) this.paint.getTextSize() * (numberOfRows -1);

        if (this.width != this.oldWidth || this.height != this.oldHeight) {
            this.oldWidth = width;
            this.oldHeight = height;
            setLayoutParams(new LinearLayout.LayoutParams(this.width, this.headerHeight + this.bodyHeight));
        }
    }

    private void moveLeft() {
        this.currentMonthCalendar.add(Calendar.MONTH, 1);
        this.updateHeaderBitmap();
        this.updateBodyBitmap();
        this.invalidate();
    }

    private void moveRight() {
        this.currentMonthCalendar.add(Calendar.MONTH, -1);
        this.updateHeaderBitmap();
        this.updateBodyBitmap();
        this.invalidate();
    }

    public void selectDay(Calendar calendar) {
        this.selectedDayCalendar = calendar;
        this.updateBodyBitmap();
        this.invalidate();

        if (this.onDateSelectListener != null) {
            this.onDateSelectListener.onDateSelected(this.selectedDayCalendar);
        }
    }

    public void setOnDateSelectListener(OnDateSelectListener onDateSelectListener) {
        this.onDateSelectListener = onDateSelectListener;
    }

    public void setDisableAllDates(boolean disableAllDates) {
        this.disableAllDates = disableAllDates;
        this.updateBodyBitmap();
        this.invalidate();
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        this.paint.setTextSize(Utils.spToPx(this.context, this.textSize));

        this.updateHeaderBitmap();
        this.updateBodyBitmap();
        this.invalidate();
    }

    public void setTitleTextColor(int titleTextColor) {
        this.titleTextColor = titleTextColor;
        this.updateHeaderBitmap();
        this.invalidate();
    }

    public void setDayTextColor(int dayTextColor) {
        this.dayTextColor = dayTextColor;
        this.updateHeaderBitmap();
        this.updateBodyBitmap();
        this.invalidate();
    }

    public void setTodayTextColor(int todayTextColor) {
        this.todayTextColor = todayTextColor;
        this.updateBodyBitmap();
        this.invalidate();
    }

    public void setSelectedDayTextColor(int selectedDayTextColor) {
        this.selectedDayTextColor = selectedDayTextColor;
        this.updateBodyBitmap();
        this.invalidate();
    }

    public void setSelectedDayBackgroundColor(int selectedDayBackgroundColor) {
        this.selectedDayBackgroundColor = selectedDayBackgroundColor;
        this.updateBodyBitmap();
        this.invalidate();
    }

    public void setDisabledDayTextColor(int disabledDayTextColor) {
        this.disabledDayTextColor = disabledDayTextColor;
        this.updateBodyBitmap();
        this.invalidate();
    }

    public int getTitleTextColor() {
        return this.titleTextColor;
    }

    public int getDayTextColor() {
        return this.dayTextColor;
    }

    public int getTodayTextColor() {
        return this.todayTextColor;
    }

    public int getSelectedDayTextColor() {
        return this.selectedDayTextColor;
    }

    public int getSelectedDayBackgroundColor() {
        return this.selectedDayBackgroundColor;
    }

    public int getDisabledDayTextColor() {
        return this.disabledDayTextColor;
    }

    public float getTextSize() {
        return this.textSize;
    }

    public boolean isDisableAllDates() {
        return this.disableAllDates;
    }

    public interface OnDateSelectListener {
        void onDateSelected(Calendar calendar);
    }

    private static final class TouchEventHelper {
        private static final int CLICK_TIME_TOLERANCE = 400;
        public static float lastX, lastY;
        public static float deltaX, deltaY;
        public static long lastClickTime;
        public static boolean hadClick;

        public static void onActionDown(MotionEvent event) {
            TouchEventHelper.lastX = event.getX();
            TouchEventHelper.lastY = event.getY();
            TouchEventHelper.lastClickTime = System.currentTimeMillis();
            TouchEventHelper.hadClick = false;
        }

        public static void onActionMove(MotionEvent event) {
            TouchEventHelper.deltaX = event.getX() - TouchEventHelper.lastX;
            TouchEventHelper.deltaY = event.getY() - TouchEventHelper.lastY;
        }

        public static void onActionUp() {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - TouchEventHelper.lastClickTime;

            TouchEventHelper.hadClick = deltaTime <= CLICK_TIME_TOLERANCE;
            TouchEventHelper.lastClickTime = currentTime;
            TouchEventHelper.lastX = 0;
        }
    }

    private static final class Utils {
        public static int spToPx(Context context, float sp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        }

        public static boolean isSameDay(Calendar a, Calendar b) {
            return a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH)
                    && a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
                    && a.get(Calendar.YEAR) == b.get(Calendar.YEAR);
        }

        public static String getDayOfWeekName(int dayOfWeek) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

            String name = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

            assert name != null;
            return name.toUpperCase(Locale.getDefault()).substring(0, 1);
        }

        public static String getMonthName(Calendar calendar) {
            return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        }

        public static String getCalendarTitle(Calendar calendar) {
            String title = Utils.getMonthName(calendar);
            return title + " " + calendar.get(Calendar.YEAR);
        }

        public static int getAmountOfDaysInMonth(Calendar calendar) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1).lengthOfMonth() + 1;
            } else {
                return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
        }

        public static int getFirstDayOfMonth(Calendar calendar) {
            Calendar firstDayCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
            return firstDayCalendar.get(Calendar.DAY_OF_WEEK);
        }
    }
}