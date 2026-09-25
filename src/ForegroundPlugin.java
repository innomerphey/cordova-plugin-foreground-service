package com.davidbriglio.foreground;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.util.Log;

public class ForegroundPlugin extends CordovaPlugin {
    private static final String TAG = "ForegroundPlugin";
    private static final String PREFS_NAME = "bt_sm_sb_fs";
    private static final String PREFS_KEY_PARAMS = "service_params";

    @Override
    @TargetApi(26)
    public boolean execute(final String action, final JSONArray args, final CallbackContext command) throws JSONException {
        if (action == null) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            Activity activity = cordova.getActivity();
            Intent intent = new Intent(activity, ForegroundService.class);

            if ("start".equals(action)) {
                intent.setAction("start");

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("title", args.getString(0));
                    jsonObject.put("text", args.getString(1));
                    jsonObject.put("icon", args.getString(2));
                    jsonObject.put("importance", args.getString(3));
                    jsonObject.put("id", args.getString(4));
                    jsonObject.put("servename", args.getString(5));
                    jsonObject.put("serviceType", args.getString(6));
                    String stringify = jsonObject.toString();
                    SharedPreferences.Editor editor =
                        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    editor.putString(PREFS_KEY_PARAMS, stringify);
                    editor.apply();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to persist service_params", e);
                }

                intent.putExtra("title", args.getString(0))
                    .putExtra("text", args.getString(1))
                    .putExtra("icon", args.getString(2))
                    .putExtra("importance", args.getString(3))
                    .putExtra("id", args.getString(4))
                    .putExtra("servename", args.getString(5))
                    .putExtra("serviceType", args.getString(6));

                activity.getApplicationContext().startForegroundService(intent);
            } else if ("stop".equals(action)) {
                // Prefer stopService so stop does not require a background startService,
                // which is restricted on modern targetSdk when the app is backgrounded.
                try {
                    SharedPreferences.Editor editor =
                        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    editor.remove(PREFS_KEY_PARAMS);
                    editor.apply();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to clear service_params on stop", e);
                }
                activity.getApplicationContext().stopService(intent);
            } else {
                return false;
            }
        }

        command.success();
        return true;
    }
}
