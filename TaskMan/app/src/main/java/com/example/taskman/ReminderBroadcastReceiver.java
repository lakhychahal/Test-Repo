package com.example.taskman;

import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String task = intent.getStringExtra("task");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task man_channel")
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle(context.getString(R.string.taskman))
                .setContentText(task)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {


            return;
        }

        notificationManager.notify(0, builder.build());
    }
}


