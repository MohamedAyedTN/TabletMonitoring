package com.solarisoffgrid.tabletmonitoring;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBootStarterReceiver extends BroadcastReceiver {
    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        Log.e("servicebg", "started on boot");
        Intent alarmIntent = new Intent(arg0, BackgroundCheckReceiver.class).addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        pendingIntent = PendingIntent.getBroadcast(arg0, 0, alarmIntent, 0);
        manager = (AlarmManager) arg0.getSystemService(Context.ALARM_SERVICE);
        int interval = arg0.getResources().getInteger(R.integer.service_loop_interval_in_milliseconds);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        arg0.sendBroadcast(alarmIntent);
        arg0.startService(new Intent(arg0, ConnectionService.class));
    }
}
