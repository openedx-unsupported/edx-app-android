#!/bin/bash

# print working directory
pwd

# run tests
echo "[INFO] running uninstallAll createProdDebugCoverageReport ..."
./gradlew uninstallAll createProdDebugCoverageReport
echo "[INFO] finished all tests"
cd ../..

# upload reports to artifacts folder
./gradlew copyUnitTestBuildArtifacts
echo "[INFO] unit test reports are being uploaded"