#!/bin/bash

# change directory to source
echo "[INFO] changing directory to android/source"
cd android/source
echo "[INFO] Changed directory to : " 
pwd

# generate build
#echo "[INFO] Running assembleProdDebug ..."
#./gradlew clean assembleProdDebug

# run tests
clear
echo "[INFO] logs cleared up to this point"
#echo "[INFO] running connectedAndroidTestProdDebug with -i option ..."
#./gradlew connectedAndroidTestProdDebug -i
#echo "[INFO] finished all tests"
echo "[INFO] running uninstallAll createProdDebugCoverageReport ..."
# remove any existing installation to avoid install failures due to different signatures
./gradlew uninstallAll createProdDebugCoverageReport
echo "[INFO] finished all tests"
