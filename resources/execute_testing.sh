#!/usr/bin/env bash

set -e

# Install edX app on emulator 
APK=$APK_PATH/"*.apk"
echo "Installing $APK"
$ANDROID_HOME/platform-tools/adb install $APK
sleep 10

# Verify if edX app is installed successfully
$ANDROID_HOME/platform-tools/adb shell pm list packages | grep org.edx.mobile
if [ $? == 0 ]; then
   echo "edX app is installed successfully"
else
   echo "edX app is installed successfully"
   exit 1
fi

#  ==============WIP==========
# Install dependencies 
