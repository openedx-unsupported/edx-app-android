#!/bin/sh

set -e

# Clear old outputs if any 
rm -rf OpenEdXMobile/build/

# Compile app 
echo 'Compiling Android App'
OUTPUT=$(./gradlew assembleProdDebuggable)

if [$OUTPUT -eq 0]; then
    echo 'Compiling Successfull. $OUTPUT'
else 
    echo 'Compiling not Successfull with following details \n $OUTPUT'
fi   

# Rename and verify the created apk 


# Archeve apk on Jenkins


# Archeve apk on HockeyApp