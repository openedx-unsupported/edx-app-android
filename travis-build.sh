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
echo "[INFO] running acceptance tests ..."
sh run-tests.sh


