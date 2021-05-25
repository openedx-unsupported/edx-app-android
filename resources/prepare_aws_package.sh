#!/usr/bin/env bash

set -e

cd $TEST_PROJECT_REPO_NAME

# prepare wheel house 
# pip wheel --wheel-dir wheelhouse -r requirements.txt

# zip pacakge for aws device farm 
zip -r my_tests.zip tests/ requirements.txt

# Move AWS Package at project root
cp my_tests.zip ../

# Move AUT(application under test) at project root
cp "../OpenEdXMobile/build/outputs/apk/prod/debuggable/$AUT_NAME" ../

cd ..
