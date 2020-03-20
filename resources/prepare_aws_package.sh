#!/usr/bin/env bash

set -e

# cd $TEST_PROJECT_REPO_NAME

# prepare wheel house 
pip wheel --wheel-dir wheelhouse -r edx-app-test/requirements.txt
sleep 60

# zip pacakge aws device farm 
zip -r test_bundle.zip edx-app-test/tests/ wheelhouse/ edx-app-test/requirements.txt
sleep 30