package com.davidbriglio.foreground;

import android.content.Intent;
import android.content.Context;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.IBinder;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.os.Build;

public class ForegroundService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            // Log an error or handle the case when the intent is null
            return START_STICKY; // or return another appropriate value
        }
        if (intent.getAction().equals("start")) {
            // Start the service
            startPluginForegroundService(intent.getExtras());
        } else {
            // Stop the service
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @TargetApi(26)
    private void startPluginForegroundService(Bundle extras) {
        Context context = getApplicationContext();
        final String TAG = "ForegroundService";

        Log.d(TAG, "Starting service with extras: " + (extras != null ? extras.toString() : "null"));

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            Log.d(TAG, "Attempting to delete channel: foreground.service.channel");
            manager.deleteNotificationChannel("foreground.service.channel");
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete channel", e);
        }

        Integer importance;
        try {
            importance = Integer.parseInt((String) extras.get("importance"));
            Log.d(TAG, "Parsed importance: " + importance);
        } catch (Exception e) {
            Log.e(TAG, "Invalid importance value, defaulting to 1", e);
            importance = 1;
        }

        switch (importance) {
            case 2:
                importance = NotificationManager.IMPORTANCE_DEFAULT;
                break;
            case 3:
                importance = NotificationManager.IMPORTANCE_HIGH;
                break;
            default:
                importance = NotificationManager.IMPORTANCE_LOW;
        }
        Log.d(TAG, "Resolved NotificationManager importance: " + importance);

        String servename = (String) extras.get("servename");
        Log.d(TAG, "Channel name (servename): " + servename);

        try {
            NotificationChannel channel = new NotificationChannel("foreground.service.channel", servename, importance);
            channel.setDescription(servename);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification channel", e);
        }

        String iconName = (String) extras.get("icon");
        int icon = getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        Log.d(TAG, "Icon name: " + iconName + ", resolved ID: " + icon);

        String title = (String) extras.get("title");
        String text = (String) extras.get("text");
        Log.d(TAG, "Notification title: " + title);
        Log.d(TAG, "Notification text: " + text);

        Notification notification = new Notification.Builder(context, "foreground.service.channel")
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(true)
                .setSmallIcon(icon == 0 ? 17301514 : icon)
                .build();

        Integer id;
        try {
            id = Integer.parseInt((String) extras.get("id"));
        } catch (Exception e) {
            Log.e(TAG, "Invalid notification ID, defaulting to 0", e);
            id = 0;
        }
        Log.d(TAG, "Notification ID: " + id);

        if (Build.VERSION.SDK_INT < 34) {
            Log.d(TAG, "Calling startForeground() without service type (SDK < 34)");
            startForeground(id != 0 ? id : 197812504, notification);
        } else {
            String fst = "";
            try {
                fst = extras.getString("serviceType");
                Log.d(TAG, "Requested foregroundServiceType: " + fst);
            } catch (Exception e) {
                Log.e(TAG, "Error reading serviceType", e);
            }

            int serviceType = getForegroundServiceType(fst);
            Log.d(TAG, "Resolved serviceType constant: " + serviceType);
            startForeground(id != 0 ? id : 197812504, notification, serviceType);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public static int getForegroundServiceType(String t){
      int it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE;
      switch(t) {
        case "FOREGROUND_SERVICE_TYPE_MANIFEST":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST;
          break;
        case "FOREGROUND_SERVICE_TYPE_NONE":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE;
          break;
        case "FOREGROUND_SERVICE_TYPE_DATA_SYNC":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
          break;
        case "FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
          break;
        case "FOREGROUND_SERVICE_TYPE_PHONE_CALL":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL;
          break;
        case "FOREGROUND_SERVICE_TYPE_LOCATION":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
          break;
        case "FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
          break;
        case "FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;
          break;
        case "FOREGROUND_SERVICE_TYPE_CAMERA":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA;
          break;
        case "FOREGROUND_SERVICE_TYPE_MICROPHONE":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
          break;
        case "111111111111":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;
          break;
        case "FOREGROUND_SERVICE_TYPE_HEALTH":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH;
          break;
        case "FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING;
          break;
        case "FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED;
          break;
        case "FOREGROUND_SERVICE_TYPE_SHORT_SERVICE":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE;
          break;
        case "FOREGROUND_SERVICE_TYPE_FILE_MANAGEMENT":
          //it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_FILE_MANAGEMENT;
          break;
        case "FOREGROUND_SERVICE_TYPE_SPECIAL_USE":
          it=android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
          break;
      }
      return it;
    }
}
