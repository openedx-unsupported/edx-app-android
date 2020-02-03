#!/usr/bin/env bash

cd .. 
cd edx-app-android-build
# cd edx-app-android-build/resources
print_message(){
echo -e "\n************************************************\n$1\n"
}

check_and_install_virtualenv(){
virtual_evn_path=$(which virtualenv)
if [ -z $virtual_evn_path ];
then
install_virtualenv
else
print_message "virtualenv is installed"
fi
}

create_or_switch_to_virtual_environment(){
virtual_env_dir="./virtual_env"
if [ -d "$virtual_env_dir" ]
then
switch_to_virtual_env
else
create_virtual_environment
switch_to_virtual_env
install_requirement_txt
fi
}

install_virtualenv(){
print_message "installing virtualenv"
pip3 install virtualenv
}

create_virtual_environment(){
print_message "creating virtual environment"
virtualenv -p /usr/bin/python3.6 ./virtual_env
}

install_requirement_txt(){
print_message "installing requirements"
pip install -r ./resources/requirements.txt
# pip install -r ./requirements.txt
sleep 20
}

switch_to_virtual_env(){
print_message "switching to virtual environment with following python version"
source "./virtual_env/bin/activate"
python --version
}

check_and_install_virtualenv
create_or_switch_to_virtual_environment
# adding aut file name manually as searching through * is behaving some times, 
# only challenge is we'll have to manually upgrade build no. in aut name on every release
AUT="$APK_PATH/*.apk"
# # AUT="$APK_PATH/edx-debuggable-2.20.2.apk"
print_message "aut value $AUT"
Test_Package="$TEST_PROJECT_REPO_NAME/test_bundle.zip"
CONFIG="edx.yml"
print_message "test package value $Test_Package"
print_message "setup AWS"
python ./resources/setup_aws.py
print_message "calling AWS Test run"
python ./resources/trigger_aws_test_run.py $AUT $Test_Package $CONFIG
