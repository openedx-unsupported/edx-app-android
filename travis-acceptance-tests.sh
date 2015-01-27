#!/bin/bash

# exit on error
set -e

# create debug build
echo "[INFO] changing directory to android/source"
cd android/source
echo "[INFO] Changed directory to : " 
pwd
echo "[INFO] running assembleProdDebug ..."
./gradlew assembleProdDebug
echo "[INFO] build created"
cd ../..

# get current timestamp, to be used in apk name
current_time=$(date "+%Y.%m.%d-%H.%M.%S")
echo "Current Time : $current_time"

# enter tests project folder
cd AcceptanceTests
pwd

# export required keys and parameters
export SAUCE_KEY="a24b5fd3-7533-4b5e-b063-aacf75956347"
export SAUCE_USERNAME="rohan-dhamal-clarice"
export APK_PATH="../android/source/VideoLocker/build/outputs/apk/VideoLocker-prod-debug.apk"
export APK_NAME="edx-prod-debug-"$current_time".apk"

echo "-----------------Configuration------------------"
echo "SAUCE_KEY=$SAUCE_KEY"
echo "SAUCE_USERNAME=$SAUCE_USERNAME"
echo "APK_PATH=$APK_PATH"
echo "APK_NAME=$APK_NAME"
echo "------------------------------------------------"

# Upload apk to SauceLabs
echo "[INFO] uploading apk file ..."
curl -u $SAUCE_USERNAME:$SAUCE_KEY -X POST -H "Content-Type: application/octet-stream" https://saucelabs.com/rest/v1/storage/$SAUCE_USERNAME/$APK_NAME?overwrite=true --data-binary @$APK_PATH
echo "[INFO] apk uploaded"

# run acceptance tests on SauceLabs in a background thread
echo "[INFO] running tests ..."
mvn test -DappPath=sauce-storage:$APK_NAME -DosVersion="4.4"  -DdeviceOS=android -DdeviceName="Android Emulator" -DtestngXml=android.xml -DsauceKey=$SAUCE_KEY -DsauceUser=$SAUCE_USERNAME -DsauceBuildName=$APK_NAME
echo "[INFO] test run finished!"
