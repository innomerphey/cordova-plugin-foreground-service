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

        // Delete notification channel if it already exists
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Prevent RuntimeException such as: Not allowed to delete channel foreground.service.channel with a foreground service
        // See: https://github.com/DavidBriglio/cordova-plugin-foreground-service/issues/25
        try {
            manager.deleteNotificationChannel("foreground.service.channel");
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Get notification channel importance
        Integer importance;

        try {
            importance = Integer.parseInt((String) extras.get("importance"));
        } catch (NumberFormatException e) {
            importance = 1;
        }

        switch(importance) {
            case 2:
                importance = NotificationManager.IMPORTANCE_DEFAULT;
                break;
            case 3:
                importance = NotificationManager.IMPORTANCE_HIGH;
                break;
            default:
                importance = NotificationManager.IMPORTANCE_LOW;
            // We are not using IMPORTANCE_MIN because we want the notification to be visible
        }

        String servename = (String) extras.get("servename");
        // Create notification channel
        NotificationChannel channel = new NotificationChannel("foreground.service.channel", servename, importance);
        channel.setDescription(servename);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        // Get notification icon
        int icon = getResources().getIdentifier((String) extras.get("icon"), "drawable", context.getPackageName());

        // Make notification
        Notification notification = new Notification.Builder(context, "foreground.service.channel")
            .setContentTitle((CharSequence) extras.get("title"))
            .setContentText((CharSequence) extras.get("text"))
            .setOngoing(true)
            .setSmallIcon(icon == 0 ? 17301514 : icon) // Default is the star icon
            .build();

        // Get notification ID
        Integer id;
        try {
            id = Integer.parseInt((String) extras.get("id"));
        } catch (NumberFormatException e) {
            id = 0;
        }

        // Put service in foreground and show notification (id of 0 is not allowed)
        if (Build.VERSION.SDK_INT < 34) {
        startForeground(id != 0 ? id : 197812504, notification);
        } else {
          String fst="";
          try {
            fst =   extras.getString("serviceType");
          } catch (Exception e) {
          }
          startForeground(id != 0 ? id : 197812504, notification,getForegroundServiceType(fst));
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
