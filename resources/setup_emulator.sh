#!/usr/bin/env bash

set -e

EXPECTED_DEVICE_NAME="emulator-5554"
BOOT_FINISHED_STATUS="stopped"
BOOT_CURRENT_STATUS=""
SYS_BOOT_CURRENT_STATUS=""
SYS_BOOT_FINISHED_STATUS="1"
ATTEMPT=0
TIMEOUT=300

kill_all_emus() {
    for emu_device in $($ANDROID_HOME/platform-tools/adb devices -l |grep 'device product:' |cut -d' ' -f1); do
        echo "Killing emulator: $emu_device"
        adb -s $emu_device emu kill
    done
}

# Kill all existing emulators if any
kill_all_emus

echo 'Creating emulator'
echo 'n' | $ANDROID_HOME/tools/bin/avdmanager create avd -n test-android -f -k "system-images;android-27;google_apis;x86"
sleep 5

echo 'loading emulator'
$ANDROID_HOME/emulator/emulator -avd test-android -no-boot-anim -no-window -noaudio & 
while true; do
    BOOT_CURRENT_STATUS=`$ANDROID_HOME/platform-tools/adb -e shell getprop init.svc.bootanim &`
    SYS_BOOT_CURRENT_STATUS=`$ANDROID_HOME/platform-tools/adb shell getprop sys.boot_completed &`    
    if [ "$BOOT_CURRENT_STATUS" = "$BOOT_FINISHED_STATUS" ] && [ "$SYS_BOOT_CURRENT_STATUS" = "$SYS_BOOT_FINISHED_STATUS" ]; 
    then 
        echo "emulator is ready"
        break
    else 
        let "ATTEMPT += 1"
        echo "$ATTEMPT sec, waiting more for emulator to start. $BOOT_CURRENT_STATUS & $SYS_BOOT_STATUS"
        if [ $ATTEMPT -gt $TIMEOUT ]; 
        then
            echo "Timeout ($TIMEOUT seconds) reached; failed to start emulator. $BOOT_CURRENT_STATUS & $SYS_BOOT_STATUS"
            exit 1
        fi
        sleep 1
    fi    
done

$ANDROID_HOME/platform-tools/adb devices |grep $EXPECTED_DEVICE_NAME
if [ $? == 0 ]; 
then
   echo "Sending Key event, to press HOME button"
   $ANDROID_HOME/platform-tools/adb shell input keyevent 3 &
   if [ $? == 0 ]; 
   then
        exit 0
   else 
        echo "The device is not responding"
        exit 1
    fi
else
   echo "The device is not accessible"
   exit 1
fi
