#!/usr/bin/env bash

cd .. 
cd edx-app-android-build
print_message(){
echo -e "\n************************************************\n$1\n"
}

virtual_env_dir="virtual_env"

check_and_install_virtualenv(){
virtual_evn_path=$(which virtualenv)
if [ -z $virtual_evn_path ];
then
install_virtualenv
else
print_message "virtualenv is installed"
fi
}

install_virtualenv(){
print_message "installing virtualenv"
pip3 install virtualenv
}

create_virtual_environment(){
if [ -d "./$virtual_env_dir" ]
then
print_message "virtual env. directory already exists."
rm -r "./$virtual_env_dir"
fi

print_message "creating virtual environment"
virtualenv -p /usr/bin/python3.6 "./$virtual_env_dir"
}

install_requirements(){
print_message "installing requirements"
pip install -r ./resources/requirements.txt
print_message "all requirements are installed"
}

switch_to_virtual_env(){
print_message "switching to virtual environment with following python version"
source "./$virtual_env_dir/bin/activate"
python --version
}

check_and_install_virtualenv
create_virtual_environment
switch_to_virtual_env
install_requirements
print_message "calling AWS Test run"
python -v ./resources/trigger_aws_test_run.py $AUT_NAME
