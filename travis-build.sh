#!/bin/bash

# print working directory
pwd

# run tests
echo "[INFO] running uninstallAll createProdDebugCoverageReport ..."
./gradlew uninstallAll createProdDebugCoverageReport
echo "[INFO] finished all tests"
cd ../..
