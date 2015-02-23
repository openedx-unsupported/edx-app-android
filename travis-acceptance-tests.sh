#!/bin/bash

# exit on error
set -e

# create debug build
pwd
echo "[INFO] running assembleProdDebug ..."
./gradlew assembleProdDebug
echo "[INFO] build created"

# get current timestamp, to be used in apk name
current_time=$(date "+%Y.%m.%d-%H.%M.%S")
echo "Current Time : $current_time"

# enter tests project folder
cd AcceptanceTests
pwd

# export required keys and parameters
# Travis Environment is supposed to provide two vairables
# SAUCE_KEY and SAUCE_USERNAME
# These two variables are used later in this shell script.
export APK_PATH="../VideoLocker/build/outputs/apk/VideoLocker-prod-debug.apk"
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

# exit from the AcceptanceTests folder
cd ..
pwd

# upload reports to artifacts folder
./gradlew copyAcceptanceTestBuildArtifacts
echo "[INFO] acceptance test reports are being uploaded"