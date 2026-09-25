package com.davidbriglio.foreground;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import org.json.JSONObject;

public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    private static final String PREFS_NAME = "bt_sm_sb_fs";
    private static final String PREFS_KEY_PARAMS = "service_params";
    private static final String CHANNEL_ID = "foreground.service.channel";
    private static final String DEFAULT_SERVICE_TYPE = "FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE";
    private static final int DEFAULT_NOTIFICATION_ID = 197812504;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Sticky restart delivers a null Intent; some OEMs may also omit action.
        // Restore saved params and call startForeground promptly to avoid
        // ForegroundServiceDidNotStartInTimeException on targetSdk 34+.
        if (intent == null || intent.getAction() == null) {
            Bundle restored = loadSavedServiceParams();
            if (restored != null) {
                startPluginForegroundService(restored);
                return START_STICKY;
            }
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if ("start".equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                extras = loadSavedServiceParams();
            }
            if (extras != null) {
                startPluginForegroundService(extras);
            } else {
                Log.e(TAG, "start action received without extras or saved params");
                stopSelf();
                return START_NOT_STICKY;
            }
        } else if ("stop".equals(action)) {
            clearSavedServiceParams();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            stopForeground(true);
        } catch (Exception e) {
            Log.e(TAG, "stopForeground in onDestroy failed", e);
        }
        super.onDestroy();
    }

    @TargetApi(26)
    private void startPluginForegroundService(Bundle extras) {
        Context context = getApplicationContext();

        Log.d(TAG, "Starting service with extras: " + (extras != null ? extras.toString() : "null"));
        if (extras == null) {
            Log.e(TAG, "Cannot start foreground service with null extras");
            stopSelf();
            return;
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Integer importance;
        try {
            importance = Integer.parseInt(String.valueOf(extras.get("importance")));
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

        String servename = extras.getString("servename");
        if (servename == null || servename.isEmpty()) {
            servename = "Foreground Service";
        }
        Log.d(TAG, "Channel name (servename): " + servename);

        // createNotificationChannel is idempotent for an existing channel id;
        // do not delete/recreate on every start (preserves user channel settings).
        try {
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, servename, importance);
                channel.setDescription(servename);
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification channel", e);
        }

        String iconName = extras.getString("icon");
        int icon = 0;
        if (iconName != null && !iconName.isEmpty()) {
            icon = getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        }
        Log.d(TAG, "Icon name: " + iconName + ", resolved ID: " + icon);

        String title = extras.getString("title");
        String text = extras.getString("text");
        Log.d(TAG, "Notification title: " + title);
        Log.d(TAG, "Notification text: " + text);

        Notification notification = new Notification.Builder(context, CHANNEL_ID)
                .setContentTitle(title != null ? title : "")
                .setContentText(text != null ? text : "")
                .setOngoing(true)
                .setSmallIcon(icon == 0 ? 17301514 : icon)
                .build();

        Integer id;
        try {
            id = Integer.parseInt(String.valueOf(extras.get("id")));
        } catch (Exception e) {
            Log.e(TAG, "Invalid notification ID, defaulting to 0", e);
            id = 0;
        }
        Log.d(TAG, "Notification ID: " + id);

        int notificationId = id != 0 ? id : DEFAULT_NOTIFICATION_ID;

        if (Build.VERSION.SDK_INT < 34) {
            Log.d(TAG, "Calling startForeground() without service type (SDK < 34)");
            startForeground(notificationId, notification);
        } else {
            String fst = extras.getString("serviceType");
            if (fst == null || fst.isEmpty()) {
                fst = DEFAULT_SERVICE_TYPE;
            }
            Log.d(TAG, "Requested foregroundServiceType: " + fst);

            int serviceType = getForegroundServiceType(fst);
            Log.d(TAG, "Resolved serviceType constant: " + serviceType);
            startForeground(notificationId, notification, serviceType);
        }
    }

    private Bundle loadSavedServiceParams() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String stringify = prefs.getString(PREFS_KEY_PARAMS, null);
        if (stringify == null || stringify.isEmpty()) {
            Log.e(TAG, "No saved service_params for sticky restart");
            return null;
        }
        try {
            JSONObject json = new JSONObject(stringify);
            Bundle extras = new Bundle();
            extras.putString("title", json.optString("title", ""));
            extras.putString("text", json.optString("text", ""));
            extras.putString("icon", json.optString("icon", ""));
            extras.putString("importance", json.optString("importance", "1"));
            extras.putString("id", json.optString("id", ""));
            extras.putString("servename", json.optString("servename", ""));
            extras.putString("serviceType", json.optString("serviceType", DEFAULT_SERVICE_TYPE));
            Log.d(TAG, "Restored service_params for sticky restart");
            return extras;
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore service_params", e);
            return null;
        }
    }

    private void clearSavedServiceParams() {
        SharedPreferences.Editor editor =
            getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(PREFS_KEY_PARAMS);
        editor.apply();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * The manifest declares only connectedDevice. Any other type makes
     * startForeground throw SecurityException on targetSdk 34+.
     */
    public static int getForegroundServiceType(String t) {
        if (t != null && !t.isEmpty() && !"FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE".equals(t)) {
            Log.w(TAG, "Ignoring foreground service type " + t + "; manifest allows connectedDevice only");
        }
        return ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
    }
}
