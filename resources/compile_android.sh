#!/usr/bin/env bash

set -e

# Clear old outputs if any
rm -rf OpenEdXMobile/build/

# Compile app
echo 'Compiling Android App'
echo -e "\n**************Java version*************\n$1\n"
java -version
./gradlew assembleProdDebug
