#!/usr/bin/env bash

set -e

cd $TEST_PROJECT_REPO_NAME

# copy requirements file 
cp tests/requirements.txt ./

# prepare wheel house 
pip wheel --wheel-dir wheelhouse -r requirements.txt

# zip pacakge aws device farm 
zip -r test_bundle.zip tests/ wheelhouse/ requirements.txt
