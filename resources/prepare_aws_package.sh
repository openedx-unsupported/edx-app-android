#!/usr/bin/env bash

set -e

cd $TEST_PROJECT_REPO_NAME

# prepare wheel house 
pip wheel --wheel-dir wheelhouse -r requirements.txt

# zip pacakge aws device farm 
zip -r test_bundle.zip tests/ wheelhouse/ requirements.txt
