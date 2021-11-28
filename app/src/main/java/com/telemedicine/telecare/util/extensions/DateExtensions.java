package com.telemedicine.telecare.util.extensions;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import com.telemedicine.telecare.base.AppController;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DateExtensions {

    private long    longTime;
    private String  date;

    public DateExtensions() { }

    public DateExtensions(String date) {
        this.date = date;
    }

    public DateExtensions(long longTime) {
        this.longTime = longTime;
    }

    public long getDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        long finalTime = 0;

        try {
            if(date == null) return 0;
            Date d = format.parse(date);
            finalTime = d != null ? d.getTime() : 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return finalTime;
    }

    public String appointmentDateFormat() {
        return new SimpleDateFormat("dd MMM, EEE", Locale.getDefault()).format(longTime);
    }

    public String getAppointmentDate() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    public String defaultDateFormat() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(longTime);
    }

    public String defaultTimeFormat() {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(longTime);
    }

    public boolean isToday(Date date) {
        if(date == null) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return format.format(date).equals(format.format(new Date()));
    }

    public boolean isSameDay(Date date1, Date date2) {
        if(date1 == null) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return format.format(date1).equals(format.format(date2 == null ? new Date() : date2));
    }

    public boolean isDateOverlap(Date start, Date end, Date date) {
        if(start == null || end == null || date == null) return false;
        if(start.compareTo(date) == 0) return true;
        return date.after(start) && date.before(end);
    }

    public Integer getAge() {
        if(date == null) return 0;
        int age = 0;
        try {
            Date birthDate = new Date();
            Date currentDate = new Date(System.currentTimeMillis());

            try {
                birthDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (birthDate != null) {
                DateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                int d1 = Integer.parseInt(formatter.format(birthDate));
                int d2 = Integer.parseInt(formatter.format(currentDate));
                age = (d2 - d1) / 10000;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return age == 0 ? null : age;
    }

    public static void showDatePicker(final View dateInputView) {
        final DatePicker datePicker = new DatePicker(AppController.getActivity());
        final int currentDay = datePicker.getDayOfMonth();
        final int currentMonth = datePicker.getMonth();
        final int currentYear = datePicker.getYear();

        DatePickerDialog datePickerDialog = new DatePickerDialog(AppController.getActivity(), (datePicker1, year, monthOfYear, dayOfMonth) ->
                ((TextView) dateInputView).setText(String.format(Locale.getDefault(), "%d/%d/%d", dayOfMonth, monthOfYear + 1, year)), currentYear, currentMonth, currentDay);

        /**
         * Set Old Date
         **/
        if (!Objects.requireNonNull(((TextView) dateInputView).getText()).toString().isEmpty()) {
            String[] oldDate = Objects.requireNonNull(((TextView) dateInputView).getText()).toString().split("/");
            int d = Integer.parseInt(oldDate[0].trim());
            int m = Integer.parseInt(oldDate[1].trim()) - 1;
            int y = Integer.parseInt(oldDate[2].trim());

            datePickerDialog.updateDate(y, m, d);
        }

        Calendar minDate = new GregorianCalendar(currentYear-125, currentMonth, currentDay);
        Calendar maxDate = new GregorianCalendar(currentYear-12, currentMonth, currentDay);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    public static void showTimePicker(final View timeInputView) {
        final TimePicker timePicker = new TimePicker(AppController.getActivity());
        final int currentHour = timePicker.getHour();
        final int currentMinute = timePicker.getMinute();

        TimePickerDialog timePickerDialog = new TimePickerDialog(AppController.getActivity(), (view, hourOfDay, minute) -> {
            try {
                String time = new SimpleDateFormat("h:mm a", Locale.US)
                        .format(Objects.requireNonNull(new SimpleDateFormat("HH:mm", Locale.US).parse(hourOfDay + ":" + minute)));
                AppExtensions.clearError(timeInputView);
                ((TextView) timeInputView).setText(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }, currentHour, currentMinute, false);

        /**
         * Set Old Time
         **/
        if (!Objects.requireNonNull(((TextView) timeInputView).getText()).toString().isEmpty()) {
            try {
                Date getTime = new SimpleDateFormat("h:mm a", Locale.US).parse(((TextView) timeInputView).getText().toString().trim());

                @SuppressLint("SimpleDateFormat") String[] oldTime = new SimpleDateFormat("HH:mm").format(Objects.requireNonNull(getTime)).split(":");
                int h = Integer.parseInt(oldTime[0].trim());
                int m = Integer.parseInt(oldTime[1].trim());

                timePickerDialog.updateTime(h, m);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        timePickerDialog.show();
    }

    private final int SECOND_MILLIS = 1000;
    private final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private final int DAY_MILLIS = 24 * HOUR_MILLIS;
    public String getTimeAgo() {
        if (longTime < 1000000000000L) {
            longTime *= 1000;
        }

        long now = System.currentTimeMillis();
        if (longTime > now || longTime <= 0) {
            return null;
        }

        final long diff = now - longTime;
        if (diff < MINUTE_MILLIS) {
            return "Active now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "Active 1 minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return "Active" + " " + (diff / MINUTE_MILLIS) + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "Active 1 hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return "Active" + " " + (diff / HOUR_MILLIS) + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Active yesterday";
        } else {
            return "Active" + " " + (diff / DAY_MILLIS) + " day ago";
        }
    }

    public boolean isTimeAllowedFoReminder() {
        Calendar c = Calendar.getInstance();
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        return hourOfDay > 6 && hourOfDay < 23;
    }

    public Date date(Integer year, Integer month, Integer day, Integer hour,  Integer minute, Integer second, Integer millisecond){
        Calendar calender = Calendar.getInstance();
        calender.set(Calendar.YEAR, year == null ? Calendar.getInstance().get(Calendar.YEAR) : year);
        calender.set(Calendar.MONTH, month == null ? Calendar.getInstance().get(Calendar.MONTH) : month);
        calender.set(Calendar.DAY_OF_MONTH, day == null ? Calendar.getInstance().get(Calendar.DAY_OF_MONTH) : day);
        calender.set(Calendar.HOUR_OF_DAY, hour == null ? 0 : hour);
        calender.set(Calendar.MINUTE, minute == null ? 0 : minute);
        calender.set(Calendar.SECOND, second == null ? 0 : second);
        calender.set(Calendar.MILLISECOND, millisecond == null ? 0 : millisecond);
        return calender.getTime();
    }

    public Date date(){
        return date(null, null, null, 0, 0, 0, 0);
    }

    public Date date(Integer year, Integer month, Integer day){
        return date(year, month, day, 0, 0, 0, 0);
    }

    public Date date(Integer hour,  Integer minute, Integer second, Integer millisecond){
        return date(null, null, null, hour, minute, second, millisecond);
    }

    public int getYears() {
        if(longTime <= 0) return 0;
        Date givenDate = new Date(longTime);
        Date currentDate = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        int d1 = Integer.parseInt(formatter.format(givenDate));
        int d2 = Integer.parseInt(formatter.format(currentDate));
        return (d2-d1) / 10000;
    }

    public List<Object[]> getAvailableTimes(Date date, String start, String end, int interval){
        List<Object[]> times = new ArrayList<>();
        if(start == null || end == null || interval <= 0) return times;

        try {
            String getDate = new DateExtensions(date.getTime()).defaultDateFormat();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault());

            Date startTime = dateFormat.parse(getDate + " " + start);
            Date endTime = dateFormat.parse(getDate + " " + end);

            if(startTime != null && endTime != null) {
                long dif = startTime.getTime();
                while (dif < endTime.getTime()) {
                    Date slot = new Date(dif);
                    Object[] time = new Object[5];
                    time[0] = slot;
                    time[1] = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(time[0]);
                    time[2] = new Date(slot.getTime() + (interval * 60000));
                    time[3] = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(time[2]);
                    time[4] = interval;
                    times.add(time);
                    dif += interval * 60000;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return times;
    }

    public List<Object[]> getAvailableDays(int month, int year){
        Calendar calendar = Calendar.getInstance();
        int curDay = 1;
        if(month == calendar.get(Calendar.MONTH)) curDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<Object[]> days = new ArrayList<>();

        for (int i=curDay; i<=maxDays; i++){
            calendar.set(Calendar.DAY_OF_MONTH, i);
            Object[] day = new Object[5];
            day[0] = calendar.get(Calendar.DAY_OF_WEEK);
            day[1] = new DateFormatSymbols().getShortWeekdays()[(int)day[0]];
            day[2] = new DateFormatSymbols().getWeekdays()[(int)day[0]];
            day[3] = calendar.get(Calendar.DAY_OF_MONTH);
            day[4] = date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).getTime();
            days.add(day);
        }

        return days;
    }

    public Object[] getCurrentDay(){
        Calendar calendar = Calendar.getInstance();
        Object[] day = new Object[5];
        day[0] = calendar.get(Calendar.DAY_OF_WEEK);
        day[1] = new DateFormatSymbols().getShortWeekdays()[(int)day[0]];
        day[2] = new DateFormatSymbols().getWeekdays()[(int)day[0]];
        day[3] = calendar.get(Calendar.DAY_OF_MONTH);
        day[4] = date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).getTime();
        return day;
    }

    public Object[] getCurrentMonth(){
        Calendar calendar = Calendar.getInstance();
        Object[] month = new Object[3];
        month[0] = calendar.get(Calendar.YEAR);
        month[1] = calendar.get(Calendar.MONTH);
        month[2] = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)];
        return month;
    }

    public List<Object[]> getNextMonths(int limit){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);

        List<Object[]> months = new ArrayList<>();

        for (int i=0; i<limit; i++){
            calendar.add(Calendar.MONTH, 1);
            Object[] month = new Object[3];
            month[0] = calendar.get(Calendar.YEAR);
            month[1] = calendar.get(Calendar.MONTH);
            month[2] = new DateFormatSymbols().getMonths()[(int) month[1]];
            months.add(month);
        }

        return months;
    }
}
