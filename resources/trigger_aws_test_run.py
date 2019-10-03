#!/usr/bin/env python

"""
This module will handle test run on AWS Device Farm
"""

import boto3
import requests


REGION = 'us-west-2'
PROJECT_NAME = 'edx-app-test'
DEVICE_POOL_NAME = 'edx_devices_pool'
ANDROID_APP_UPLOAD_TYPE = 'ANDROID_APP'
PACKAGE_UPLOAD_TYPE = 'APPIUM_PYTHON_TEST_PACKAGE'
CUSTOM_SPECS_UPLOAD_TYPE = 'APPIUM_PYTHON_TEST_SPEC'
RUN_TYPE = 'APPIUM_PYTHON'
RUN_NAME = 'edX test run'

AUT_NAME = 'edx.apk'
PACKAGE_NAME = 'test_bundle.zip'
CUSTOM_SPECS_NAME = 'edx.yml'

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
    print('{} uploaded successfully'.format(AUT_NAME))

    package_arn = upload_file(project_arn,
                              PACKAGE_UPLOAD_TYPE,
                              PACKAGE_NAME
                             )
    print('{} uploaded successfully'.format(PACKAGE_NAME))

    test_specs_arn = upload_file(project_arn,
                                 CUSTOM_SPECS_UPLOAD_TYPE,
                                 CUSTOM_SPECS_NAME
                                )
    print('{} uploaded successfully'.format(CUSTOM_SPECS_NAME))

    device_pool_arn = get_device_pool(project_arn, DEVICE_POOL_NAME)

    test_run_arn = schedule_run(
        project_arn=project_arn,
        name=RUN_NAME,
        device_pool_arn=device_pool_arn,
        app_arn=aut_arn,
        test_package_arn=package_arn,
        test_specs_arn=test_specs_arn)

    get_test_run(test_run_arn)


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

    result = device_farm.schedule_run(
        projectArn=project_arn,
        appArn=app_arn,
        devicePoolArn=device_pool_arn,
        name=name,
        test={'type': RUN_TYPE,
              'testPackageArn': test_package_arn,
              'testSpecArn': test_specs_arn
             })

    run = result['run']
    return run['arn']


def get_test_run(run_arn):
    """
    get test run details

    Arguments:
            run_arn (str): run arn
    """

    test_run = device_farm.get_run(arn=run_arn)

    run_name = test_run['run']['name']
    run_platform = test_run['run']['platform']
    run_created = test_run['run']['created']
    run_status = test_run['run']['status']
    run_results = test_run['run']['result']

    print('{} on {} is created at {} with status - {} & results - {}'.format(run_name,
                                                                             run_platform,
                                                                             str(run_created),
                                                                             run_status,
                                                                             run_results
                                                                            ))

if __name__ == '__main__':
    aws_job()
