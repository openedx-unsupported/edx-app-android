#!/usr/bin/env bash

set -e

# validated if created build is debug build 
EXPECTED_FLAG='application-debuggable'

for APK in $APK_PATH/"*.apk"; do
    echo "Checking if $APK is debuggable"
    DEBUGGABLE=`$ANDROID_HOME/build-tools/28.0.3/aapt dump badging $APK |grep $EXPECTED_FLAG`
    if [ $? == 0 ]; then
       echo "The build is debuggable"
       exit 0
    else
       echo "The build is not debuggable"
       exit 1
    fi
done 
