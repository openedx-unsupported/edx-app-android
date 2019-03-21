#!/usr/bin/env bash

set -e

EXPECTED_DEVICE_NAME="emulator-5554"

# Connect Amazon Device Form 
# This part will be WIP


# Verify if device is available 
$ANDROID_HOME/platform-tools/adb devices |grep $EXPECTED_DEVICE_NAME
if [ $? == 0 ]; 
then
   echo "Sending Key event, to press HOME button"
   $ANDROID_HOME/platform-tools/adb shell input keyevent 3 &
   if [ $? == 0 ]; 
   then
        echo "The device looks good"
   else 
        echo "The device is not responding"
        exit 1
    fi
else
   echo "The device is not accessible"
   exit 1
fi

# Install edX app on emulator 
APK=$APK_PATH/"*.apk"
echo "Installing $APK"
$ANDROID_HOME/platform-tools/adb install $APK
sleep 10

# Verify if edX app is installed successfully
$ANDROID_HOME/platform-tools/adb shell pm list packages | grep org.edx.mobile
if [ $? == 0 ]; then
   echo "edX app is installed successfully"
   exit 0
else
   echo "edX app is not installed successfully"
   exit 1
fi
