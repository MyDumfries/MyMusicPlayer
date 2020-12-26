package com.mydumfries.mymusicplayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;

import java.util.Calendar;
import java.util.Date;

public class Alarm extends BroadcastReceiver {
    public static final String SETTINGS = "Settings";
    public static final String ALARMYESNO = "AlarmYesNo";
    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";
    public static final String SATURDAY = "Saturday";
    public static final String SUNDAY = "Sunday";
    public static final String MONDAY = "Monday";
    public static final String TUESDAY = "Tuesday";
    public static final String WEDNESDAY = "Wednesday";
    public static final String THURSDAY = "Thursday";
    public static final String FRIDAY = "Friday";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences Settings = context.getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        String alarmyesno = Settings.getString(ALARMYESNO, "no");
        if (alarmyesno.equals("yes")) {
            AudioManager am = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
            Date dat = new Date();// initializes to now
            Calendar cal_now = Calendar.getInstance();
            cal_now.setTime(dat);
            int day = cal_now.get(Calendar.DAY_OF_WEEK);
            String yesno;
            switch (day) {
                case 1:
                    //Sunday
                    yesno = Settings.getString(SUNDAY, "no");
                    if (yesno.equals("yes")) {
                        Intent player = new Intent(context, PlayPhoneMusic.class);
                        player.putExtra("source", 1);
                        player.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(player);
                    }
                    break;
                case 2:
                    //Monday
                    yesno = Settings.getString(MONDAY, "no");
                    if (yesno.equals("yes")) {
                        Intent player = new Intent(context, PlayPhoneMusic.class);
                        player.putExtra("source", 1);
                        player.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(player);
                    }
                    break;
                case 3:
                    //Tuesday
                    yesno = Settings.getString(TUESDAY, "no");
                    if (yesno.equals("yes")) {
                        Intent player = new Intent(context, PlayPhoneMusic.class);
                        player.putExtra("source", 1);
                        player.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(player);
                    }
                    break;
                case 4:
                    //Wednesday
                    yesno = Settings.getString(WEDNESDAY, "no");
                    if (yesno.equals("yes")) {
                        Intent player = new Intent(context, PlayPhoneMusic.class);
                        player.putExtra("source", 1);
                        player.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(player);
                    }
                    break;
                case 5:
                    //Thursday
                    yesno = Settings.getString(THURSDAY, "no");
                    if (yesno.equals("yes")) {
                        Intent player = new Intent(context, PlayPhoneMusic.class);
                        player.putExtra("source", 1);
                        player.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(player);
                    }
                    break;
                case 6:
                    //Friday
                    yesno = Settings.getString(FRIDAY, "no");
                    if (yesno.equals("yes")) {
                        Intent player = new Intent(context, PlayPhoneMusic.class);
                        player.putExtra("source", 1);
                        player.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(player);
                    }
                    break;
                case 7:
                    //Saturday
                    yesno = Settings.getString(SATURDAY, "no");
                    if (yesno.equals("yes")) {
                        Intent player = new Intent(context, PlayPhoneMusic.class);
                        player.putExtra("source", 1);
                        player.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(player);
                    }
                    break;
            }//end switch
        }//end alarmyesno
    }

    public void SetAlarm(Context context) {
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        Date dat = new Date();// initializes to now
        Calendar cal_alarm = Calendar.getInstance();
        SharedPreferences Settings = context.getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        int hour = Settings.getInt(HOUR, 6);
        int minute = Settings.getInt(MINUTE, 39);
        cal_alarm.setTime(dat);
        cal_alarm.set(Calendar.HOUR_OF_DAY, hour);// set the alarm time
        cal_alarm.set(Calendar.MINUTE, minute);
        cal_alarm.set(Calendar.SECOND, 1);
        long alarmtime = cal_alarm.getTimeInMillis();
        Calendar timenow = Calendar.getInstance();
        timenow.setTime(dat);
        long thetimenow = timenow.getTimeInMillis();
        if (alarmtime < thetimenow) {
            cal_alarm.add(Calendar.DATE, 1);
        }
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }

    public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}