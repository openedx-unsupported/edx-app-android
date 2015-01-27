#!/bin/bash

# change directory to source
echo "[INFO] changing directory to android/source"
cd android/source
echo "[INFO] Changed directory to : " 
pwd

# run tests
echo "[INFO] running uninstallAll createProdDebugCoverageReport ..."
./gradlew uninstallAll createProdDebugCoverageReport
echo "[INFO] finished all tests"

# run Acceptance Tests
cd ../..
cd AcceptanceTests
sh run-tests.sh &
echo "[INFO] acceptance tests are running on another thread"

