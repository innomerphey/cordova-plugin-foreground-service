var exec = require('cordova/exec');

module.exports = {
    start: function(title, text, icon, importance, notificationId, servename) {
        exec(null, null, "ForegroundPlugin", "start", [title || "", text || "", icon || "", importance || "1", notificationId || "", servename || ""]);
  },
  stop: function() {
    exec(null, null, "ForegroundPlugin", "stop", []);
  }
};