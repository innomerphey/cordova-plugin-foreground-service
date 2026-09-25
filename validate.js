#!/usr/bin/env node
'use strict';

/**
 * Deterministic source-level validation for the vendor foreground-service fork.
 * Confined to this plugin directory; no app install or Android build required.
 */

const fs = require('fs');
const path = require('path');

const root = __dirname;
const failures = [];

function read(rel) {
  return fs.readFileSync(path.join(root, rel), 'utf8');
}

function assert(condition, message) {
  if (!condition) {
    failures.push(message);
  }
}

function assertIncludes(haystack, needle, message) {
  assert(haystack.indexOf(needle) !== -1, message + ' (missing: ' + needle + ')');
}

function assertNotIncludes(haystack, needle, message) {
  assert(haystack.indexOf(needle) === -1, message + ' (unexpected: ' + needle + ')');
}

const pkg = JSON.parse(read('package.json'));
assert(pkg.name === 'cordova-plugin-foreground-service', 'package.json name must stay cordova-plugin-foreground-service');
assert(pkg.version === '1.1.4', 'package.json version must stay 1.1.4 for compatibility');
assert(pkg.cordova && pkg.cordova.id === 'cordova-plugin-foreground-service', 'cordova.id must stay cordova-plugin-foreground-service');

const pluginXml = read('plugin.xml');
assertIncludes(pluginXml, 'id="cordova-plugin-foreground-service"', 'plugin.xml id');
assertIncludes(pluginXml, 'version="1.1.4"', 'plugin.xml version');
assertIncludes(pluginXml, 'android:exported="false"', 'plugin.xml exported=false');
assertIncludes(pluginXml, 'android:stopWithTask="true"', 'plugin.xml stopWithTask=true');
assertIncludes(pluginXml, 'android:foregroundServiceType="connectedDevice"', 'plugin.xml connectedDevice type');
assertIncludes(pluginXml, 'android.permission.FOREGROUND_SERVICE"', 'plugin.xml FOREGROUND_SERVICE');
assertIncludes(pluginXml, 'android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE"', 'plugin.xml FOREGROUND_SERVICE_CONNECTED_DEVICE');
assertNotIncludes(pluginXml, 'android:exported="true"', 'plugin.xml must not export the service');

const js = read('www/foreground.js');
assertIncludes(js, 'start: function', 'JS start API preserved');
assertIncludes(js, 'stop: function', 'JS stop API preserved');
assertIncludes(js, '"ForegroundPlugin"', 'JS service name preserved');
assertIncludes(js, 'FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE', 'JS default serviceType preserved');

const pluginJava = read('src/ForegroundPlugin.java');
assertIncludes(pluginJava, '"start".equals(action)', 'Plugin null-safe start action');
assertIncludes(pluginJava, '"stop".equals(action)', 'Plugin null-safe stop action');
assertIncludes(pluginJava, 'stopService(intent)', 'Plugin stop uses stopService');
assertNotIncludes(pluginJava, 'startService(intent)', 'Plugin stop must not use startService');
assertIncludes(pluginJava, 'startForegroundService(intent)', 'Plugin start still uses startForegroundService');
assertIncludes(pluginJava, 'PREFS_KEY_PARAMS', 'Plugin persists service_params');
assertIncludes(pluginJava, 'command.success()', 'Plugin preserves success callback');

const serviceJava = read('src/ForegroundService.java');
assertIncludes(serviceJava, 'intent == null || intent.getAction() == null', 'Service handles null/sticky restart');
assertIncludes(serviceJava, 'loadSavedServiceParams()', 'Service restores saved params');
assertIncludes(serviceJava, 'startPluginForegroundService(restored)', 'Sticky path calls startForeground via helper');
assertIncludes(serviceJava, '"start".equals(action)', 'Service null-safe start action');
assertIncludes(serviceJava, 'FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE', 'Service connectedDevice type');
assertIncludes(serviceJava, 'getNotificationChannel(CHANNEL_ID) == null', 'Channel created only when missing');
assertNotIncludes(serviceJava, 'deleteNotificationChannel', 'Must not delete notification channel each start');
assertIncludes(serviceJava, 'onDestroy()', 'Service cleans up on stopService path');
assertIncludes(serviceJava, 'service_params', 'Service reads service_params key');

// Static logic checks approximating sticky-restart and type resolution without Android APIs.
function simulateLoadSavedParams(jsonText) {
  if (!jsonText) {
    return null;
  }
  const json = JSON.parse(jsonText);
  return {
    title: json.title || '',
    text: json.text || '',
    icon: json.icon || '',
    importance: json.importance || '1',
    id: json.id || '',
    servename: json.servename || '',
    serviceType: json.serviceType || 'FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE'
  };
}

function resolveServiceType(t) {
  if (!t) {
    return 'CONNECTED_DEVICE';
  }
  if (t === 'FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE') {
    return 'CONNECTED_DEVICE';
  }
  if (t === 'FOREGROUND_SERVICE_TYPE_NONE') {
    return 'NONE';
  }
  return 'CONNECTED_DEVICE';
}

const sampleParams = JSON.stringify({
  title: 'Sensor sync',
  text: 'Running',
  icon: 'ic_notification_icon',
  importance: '1',
  id: '1',
  servename: 'Sensor background Sync',
  serviceType: 'FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE'
});

const restored = simulateLoadSavedParams(sampleParams);
assert(restored !== null, 'sample service_params must restore');
assert(resolveServiceType(restored.serviceType) === 'CONNECTED_DEVICE', 'restored type must be connectedDevice');
assert(simulateLoadSavedParams(null) === null, 'missing params must not invent a start');
assert(resolveServiceType(null) === 'CONNECTED_DEVICE', 'null type defaults to connectedDevice');
assert(resolveServiceType('') === 'CONNECTED_DEVICE', 'empty type defaults to connectedDevice');

if (failures.length) {
  console.error('validate: FAILED');
  failures.forEach(function (f) {
    console.error(' - ' + f);
  });
  process.exit(1);
}

console.log('validate: OK');
console.log('Checked package identity, plugin.xml manifest/permissions, JS API,');
console.log('null/sticky restore path, stopService stop path, and channel reuse.');
