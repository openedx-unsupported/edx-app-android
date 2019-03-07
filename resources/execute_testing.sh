#!/usr/bin/env bash

set -e

PYTHON_ENVIRONMENT_NAME="python_env"
NODE_ENVIRONMENT_NAME="node_env"

# setup python virutal env and install dependencies
virtualenv "$PYTHON_ENVIRONMENT_NAME"
source "./$PYTHON_ENVIRONMENT_NAME/bin/activate"
pip install -r ./edx-app-test/requirements.txt
pip install nodeenv

# setup node virutal env and install dependencies
nodeenv "$NODE_ENVIRONMENT_NAME"
source "./$NODE_ENVIRONMENT_NAME/bin/activate"
npm install appium 

# start appium server 
./node_modules/.bin/appium & 
sleep 5

# start execution 
pytest -v 
