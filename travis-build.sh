#!/bin/bash

# change directory to source
echo "[INFO] changing directory to android/source"
cd android/source
echo "[INFO] Changed directory to : " 
pwd

# run tests
echo "[INFO] running uninstallAll createProdDebugCoverageReport ..."
./gradlew createProdDebugCoverageReport
echo "[INFO] finished all tests"
