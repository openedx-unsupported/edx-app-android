#!/usr/bin/env bash

set -e

cd $TEST_PROJECT_REPO_NAME

# zip pacakge for aws device farm for python3
zip -r test_bundle.zip tests/ requirements.txt

# Move AWS Package at project root
cp test_bundle.zip ../

# Move AUT(application under test) at project root
cp "../OpenEdXMobile/build/outputs/apk/prod/debuggable/$AUT_NAME" ../

cd ..
