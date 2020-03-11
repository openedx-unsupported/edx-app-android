#!/usr/bin/env python

"""
This module will handle test run on AWS Device Farm
"""

import os
import sys
import errno
import boto3
import requests
import time
import setup_aws

REGION = 'us-west-2'
PROJECT_NAME = 'edx-app-test'
DEVICE_POOL_NAME = 'edx_devices_pool'
ANDROID_APP_UPLOAD_TYPE = 'ANDROID_APP'
PACKAGE_UPLOAD_TYPE = 'APPIUM_PYTHON_TEST_PACKAGE'
CUSTOM_SPECS_UPLOAD_TYPE = 'APPIUM_PYTHON_TEST_SPEC'
RUN_TYPE = 'APPIUM_PYTHON'
RUN_NAME = 'edX_test_run'
RUN_TIMEOUT_SECONDS = 60 * 30
UPLOAD_SUCCESS_STATUS = 'SUCCEEDED'
RUN_COMPLETED_STATUS = 'COMPLETED'
TEST_PROJECT_REPO_NAME = 'edx-app-test'
CUSTOM_SPECS_NAME ='edx.yml'
APK_PATH = './OpenEdXMobile/build/outputs/apk/prod/debuggable/'
AUT_NAME = APK_PATH + 'edx-debuggable-2.20.2.apk'	
PACKAGE_NAME = TEST_PROJECT_REPO_NAME + '/test_bundle.zip'

print('Application Under Test - {}, Test Package - {} - configs {}'.format(
        AUT_NAME,
        PACKAGE_NAME,
        CUSTOM_SPECS_NAME
    ))

setup_aws.setup_aws_data()
device_farm = boto3.client('devicefarm', region_name=REGION)


def aws_job():
    """
    aws job to manage test run
    """

    project_arn = get_project_arn(PROJECT_NAME)
    aut_arn = upload_file(project_arn,
                          ANDROID_APP_UPLOAD_TYPE,
                          AUT_NAME
                          )
    package_arn = upload_file(project_arn,
                              PACKAGE_UPLOAD_TYPE,
                              PACKAGE_NAME
                              )
    test_specs_arn = upload_file(project_arn,
                                 CUSTOM_SPECS_UPLOAD_TYPE,
                                 CUSTOM_SPECS_NAME
                                 )
    device_pool_arn = get_device_pool(project_arn, DEVICE_POOL_NAME)

    test_run_arn = schedule_run(
        project_arn=project_arn,
        name=RUN_NAME,
        device_pool_arn=device_pool_arn,
        app_arn=aut_arn,
        test_package_arn=package_arn,
        test_specs_arn=test_specs_arn)

    get_test_run(test_run_arn)

    get_test_run_artifacts(RUN_NAME, test_run_arn)


def get_project_arn(project_name):
    """
    get project arn

    Arguments:
            project_arn (str): project arn

    Return:
            str: project arn value

    """

    for project in device_farm.list_projects()['projects']:
        if project['name'] == project_name:
            return project['arn']
    raise KeyError('Could not find project %r' % project_name)


def upload_file(project_arn, upload_type, target_file_name):
    """
    intiate upload and return arn

    Arguments:
            project_arn (str): project arn
            upload_type (str): upload type
            target_file_name (str): file name to upload

    Return:
            str: uploaded arn

    """

    upload_response = device_farm.create_upload(
        projectArn=project_arn,
        name=target_file_name,
        type=upload_type)

    upload_arn = upload_response['upload']['arn']
    status = upload_response['upload']['status']
    name = upload_response['upload']['name']
    pre_signed_url = upload_response['upload']['url']
    print('File {} - status {} '.format(name, status))

    _upload_presigned_url(pre_signed_url, name)

    timeout_seconds = 10
    check_every_seconds = 10 if timeout_seconds == RUN_TIMEOUT_SECONDS else 1
    start = time.time()
    while True:
        get_upload_status = device_farm.get_upload(arn=upload_arn)
        current_status = get_upload_status['upload']['status']
        if current_status in UPLOAD_SUCCESS_STATUS:
            print('{} upload status - {}'.format(
                target_file_name,
                current_status
            )
            )
            break
        print(
            'Waiting for upload to complete, current status - {}'.format(
                current_status
            )
        )
        now = time.time()
        if now - start > timeout_seconds:
            print('Time out, unable to upload {}'.format(target_file_name))
            break
        time.sleep(check_every_seconds)

    print('{} uploaded successfully'.format(target_file_name))
    return upload_arn


def _upload_presigned_url(url, target_file_name):
    """
    upload presigned url of specific upload

    Arguments:
            url (str): presinged url of specific upload
            target_file_name (str): file name to upload
    """

    with open(target_file_name, 'rb') as target_file:
        data = target_file.read()
        result = requests.put(url, data=data)
        assert result.status_code == 200


def get_device_pool(project_arn, name):
    """
    get device pool

    Arguments:
            project_arn (str): project arn
            name (str): name of pool

    Return:
            str: device pool arn
    """

    for device_pool in device_farm.list_device_pools(arn=project_arn
                                                     )['devicePools']:
        if device_pool['name'] == name:
            print('{} exits '.format(name))
            return device_pool['arn']

    raise KeyError('Could not find device pool %r' % name)


def schedule_run(project_arn, name, device_pool_arn, app_arn,
                 test_package_arn, test_specs_arn):
    """
    schedule test run

    Arguments:
            project_arn (str): project arn
            name (str): name of run
            device_pool_arn (str): device pool arn
            app_arn (str): target app arn
            test_package_arn (str): test package arn

    Return:
            str: test run arn

    """

    schedule_run_result = device_farm.schedule_run(
        projectArn=project_arn,
        appArn=app_arn,
        devicePoolArn=device_pool_arn,
        name=name,
        test={'type': RUN_TYPE,
              'testPackageArn': test_package_arn,
              'testSpecArn': test_specs_arn,
              },
    )

    run_arn = schedule_run_result['run']['arn']

    test_run = device_farm.get_run(arn=run_arn)
    print('Run Name - {} on {} is started at {} with status - {} & results - {}'.format(
        test_run['run']['name'],
        test_run['run']['platform'],
        str(test_run['run']['created']),
        test_run['run']['status'],
        test_run['run']['result']
    )
    )
    return run_arn


def get_test_run(run_arn):
    """
    wait for run to complete

    Arguments:
            run_arn (str): run arn
    """

    timeout_seconds = RUN_TIMEOUT_SECONDS
    check_every_seconds = 30
    wait_try = 1
    start = time.time()
    while True:
        get_run_details = device_farm.get_run(arn=run_arn)
        run_status = get_run_details['run']['status']
        run_result = get_run_details['run']['result']
        if run_status in RUN_COMPLETED_STATUS:
            print('Run is {} with {} result'.format(run_status, run_result))
            break
        print('{} - Waiting for run to finish, currently in {} status with {} result '.format(
                wait_try,
                run_status,
                run_result))
        now = time.time()
        if now - start > timeout_seconds:
            print(now - start)
            print('{} - Time out, unable to finish run'.format(wait_try))
            break
        time.sleep(check_every_seconds)
        wait_try += 1


def get_test_run_artifacts(run_name, run_arn):
    """
    get run artifacts

    Arguments:
            run_name (str): run name
            run_arn (str): run arn
    """

    run_artifacts_dict = device_farm.list_artifacts(
        arn=run_arn, type='FILE')['artifacts']
    if len(run_artifacts_dict) > 0:
        print('Total {} artifacts are found'.format(len(run_artifacts_dict)))
        for run_artifacts in run_artifacts_dict:
            artifacts_name = run_artifacts['name']
            artifacts_type = run_artifacts['type']
            artifacts_extension = run_artifacts['extension']
            artifacts_url = run_artifacts['url']
            download_artifacts(
                artifacts_name,
                artifacts_type,
                artifacts_extension,
                artifacts_url)
    else:
        print('No artifacts are found for given run')
    print('All artifacts are downloaded')


def download_artifacts(file_name, file_type, file_extension, file_url):
    """
    get run artifacts

    Arguments:
            file_name (str): file name
            file_type (str): file type
            file_extension (str): file extension
            file_url (str): file url
    """

    work_directory = os.getcwd()
    artifacts_directory = work_directory + "/" + RUN_NAME + " artifacts"
    if not os.path.exists(artifacts_directory):
        try:
            os.mkdir(artifacts_directory)
            print('Artifacts directory created - {} '.format(
                artifacts_directory
            )
            )
        except OSError as exc:
            if exc.errno != errno.EEXIST:
                raise
    target_file_name = artifacts_directory + "/" + file_name + "." + file_extension
    server_response = requests.request("GET", file_url)
    with open(target_file_name, "wb") as target_file:
        target_file.write(server_response.content)
        target_file.close()
    print('File type - {} with name - {} is downloaded successfully'.format(
        file_type,
        target_file_name
    )
    )


if __name__ == '__main__':
    aws_job()
