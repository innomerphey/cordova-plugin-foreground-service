var exec = require('cordova/exec');

module.exports = {
  // serviceType value
  // FOREGROUND_SERVICE_TYPE_MANIFEST,
  // FOREGROUND_SERVICE_TYPE_NONE,
  // FOREGROUND_SERVICE_TYPE_DATA_SYNC,
  // FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
  // FOREGROUND_SERVICE_TYPE_PHONE_CALL,
  // FOREGROUND_SERVICE_TYPE_LOCATION,
  // FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
  // FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
  // FOREGROUND_SERVICE_TYPE_CAMERA,
  // FOREGROUND_SERVICE_TYPE_MICROPHONE,
  // FOREGROUND_SERVICE_TYPE_HEALTH,
  // FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING,
  // FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED,
  // FOREGROUND_SERVICE_TYPE_SHORT_SERVICE,
  // FOREGROUND_SERVICE_TYPE_FILE_MANAGEMENT,
  // FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
  start: function(title, text, icon, importance, notificationId, servename,serviceType) {
        
        exec(null, null, "ForegroundPlugin", "start", [title || "", text || "", icon || "", importance || "1", notificationId || "", servename || "", serviceType || "FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE"]);
  },
  stop: function() {
    exec(null, null, "ForegroundPlugin", "stop", []);
  }
};