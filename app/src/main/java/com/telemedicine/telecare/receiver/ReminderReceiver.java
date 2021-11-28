package com.telemedicine.telecare.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import com.telemedicine.telecare.util.extensions.NotificationExtensions;

/**
 *  Reminder Service
 **/
public class ReminderReceiver extends BroadcastReceiver {
    NotificationExtensions extensions;

    @Override public void onReceive(final Context context, Intent intent) {
        if(!new DateExtensions().isTimeAllowedFoReminder()) return;
        extensions = new NotificationExtensions(context);

        String time = new DateExtensions(System.currentTimeMillis()).defaultTimeFormat();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context,0,intent,0);

        extensions.showNotification(time, "Time to drink water", contentIntent);
    }

    public void startReminder(Context context){
        try {
            Intent myIntent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            assert alarmManager != null;
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), Constants.REMINDER_DELAY, pendingIntent);
        }
        catch (Exception ex){
            ex.printStackTrace();
            Log.e(Constants.TAG, "Sorry, there is some problem in starting Tele Consultant, please! reinstall it");
        }
    }

    public void stopReminder(Context context){
        Intent myIntent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null) alarmManager.cancel(pendingIntent);
    }
}
