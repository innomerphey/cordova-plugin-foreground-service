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
    @Override
    @TargetApi(26)
    public boolean execute (final String action, final JSONArray args, final CallbackContext command) throws JSONException {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            Activity activity = cordova.getActivity();
            Intent intent = new Intent(activity, ForegroundService.class);

            if (action.equals("start")) {
                // Tell the service we want to start it
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
                  SharedPreferences.Editor editor = activity.getSharedPreferences("bt_sm_sb_fs", Context.MODE_PRIVATE).edit();
                  editor.putString("service_params", stringify);
                  editor.apply();
                }catch (Exception errrr){}
                // Pass the notification title/text/icon to the service
                intent.putExtra("title", args.getString(0))
                    .putExtra("text", args.getString(1))
                    .putExtra("icon", args.getString(2))
                    .putExtra("importance", args.getString(3))
                    .putExtra("id", args.getString(4))
                    .putExtra("servename", args.getString(5))        
                    .putExtra("serviceType", args.getString(6))                                   
                    ;
                
                // Start the service
                activity.getApplicationContext().startForegroundService(intent);
            } else if (action.equals("stop")) {
                // Tell the service we want to stop it
                intent.setAction("stop");

                // Stop the service
                activity.getApplicationContext().startService(intent);
            }
        }

        command.success();
        return true;
    }
}
