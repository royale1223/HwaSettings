#!/bin/bash

ant clean
ant release
adb shell mount -o rw,remount /system
adb shell rm /system/app/HwaSettings.apk
sleep 2
adb push ./bin/HwaSettings-release.apk /system/app/HwaSettings.apk
sleep 2
adb shell mount -o ro,remount /system
sleep 3
adb shell am start -n com.cyanogenmod.settings.device.hwa/.HwaSettingsActivity
exit 0
