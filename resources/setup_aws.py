#!/usr/bin/env python

"""
This module will setup environment for AWS Device Farm
"""

import boto3
import requests
import sys

REGION = 'us-west-2'
PROJECT_NAME = 'edx-app-test'
DEVICE_POOL_NAME = 'edx_devices_pool'
TARGET_AVAILABILITY = 'HIGHLY_AVAILABLE'

status_flag = False
device_farm = boto3.client('devicefarm', REGION)


def setup_aws_data():
    """
    setup data needed for AWS Device Farm
    """

    target_project_arn = setup_project(PROJECT_NAME)
    device_pool_arn = setup_device_pool(target_project_arn, DEVICE_POOL_NAME)
    get_device_info(target_project_arn)


def setup_project(project_name):
    """
    Check if specific project exists, if not create new one by given name

    Arguments:
            project_name (str): project name

    Return:
            str: project arn value

    """

    project_arn = ''
    for project in device_farm.list_projects()['projects']:
        if project['name'] == project_name:
            print('{} project already exists'.format(project_name))
            project_arn = project['arn']
        else:
            print(
                '{} project is not available, creating new one'.format(
                    project_name
                )
            )
            project_arn = create_project(project_name)

        return project_arn

    raise KeyError('Problem finding project %r' % project_name)


def create_project(project_name):
    """
    Create new Project

    Arguments:
            project_name (str): name of project to create

    Return:
            str: project arn value

    """

    new_project = device_farm.create_project(
        name=project_name,
        defaultJobTimeoutMinutes=123)
    if new_project is not None:
        project_name = new_project['project']['name']
        print('{} project created successfully'.format(project_name))
        return new_project['project']['arn']
    else:
        print('Problem creating {} project'.format(project_name))


def setup_device_pool(project_arn, device_pool_name):
    """
    Check if specific device pool exists, if not create new one by given name

    Arguments:
            project_arn (str): project arn
            name (str): name of pool

    Return:
            str: device pool arn
    """

    target_device_pool_arn = ''
    is_device_pool_exists = False
    for device_pool in device_farm.list_device_pools(arn=project_arn)[
            'devicePools']:
        pool_name = device_pool['name']
        if pool_name == device_pool_name:
            print('{} already exists'.format(pool_name))
            target_device_pool_arn = device_pool['arn']
            is_device_pool_exists = True
            break
        else:
            is_device_pool_exists = False

    if not is_device_pool_exists:
        target_device_pool_arn = create_device_pool(
            device_pool_name, project_arn)

    return target_device_pool_arn

    raise KeyError('Problem finding device pool %r' % device_pool_name)


def create_device_pool(pool_name, project_arn):
    """
    Create new device pool

    Arguments:
            pool_name (str): name of pool to create

    Return:
            str: pool arn value

    """

    new_device_pool = device_farm.create_device_pool(
        projectArn=project_arn,
        name=pool_name,
        description='it is edX device pool',
        maxDevices=1,
        rules=[
            {
                "attribute": "PLATFORM",
                "operator": "EQUALS",
                "value": '"ANDROID"'
            },
            {
                "attribute": "OS_VERSION",
                "operator": "GREATER_THAN_OR_EQUALS",
                "value": '"9"'
            },
            {
                "attribute": "MANUFACTURER",
                "operator": "EQUALS",
                "value": '"Google"'
            },
            {
                "attribute": "AVAILABILITY",
                "operator": "EQUALS",
                "value": '"HIGHLY_AVAILABLE"'
            },
            {
                "attribute": "FLEET_TYPE",
                "operator": "EQUALS",
                "value": '"PUBLIC"'
            }
        ]
    )
    if new_device_pool is not None:
        new_pool_name = new_device_pool['devicePool']['name']
        new_pool_arn = new_device_pool['devicePool']['arn']
        print('{} is created successfully'.format(pool_name))
        return new_pool_arn
    else:
        print('Problem creating {} device pool'.format(project_name))


def get_device_info(target_project_arn):
    """
    Check if specific device pool exists, if not create new one by given name

    Arguments:
            target_project_arn (str): project arn

    """
    try:
        device_info = device_farm.list_devices(
            arn=target_project_arn,
            filters=[
                {
                    "attribute": "PLATFORM",
                    "operator": "EQUALS",
                    "values": ['ANDROID', ]
                },
                {
                    "attribute": "OS_VERSION",
                    "operator": "GREATER_THAN_OR_EQUALS",
                    "values": ['9', ]
                },
                {
                    "attribute": "MANUFACTURER",
                    "operator": "EQUALS",
                    "values": ['Google', ]
                },
                {
                    "attribute": "AVAILABILITY",
                    "operator": "EQUALS",
                    "values": ['HIGHLY_AVAILABLE', ]
                },
                {
                    "attribute": "FLEET_TYPE",
                    "operator": "EQUALS",
                    "values": ['PUBLIC', ]
                }
            ])['devices']

        if device_info is not None:
            device_arn = device_info[0]['arn']
            device_name = device_info[0]['name']
            device_manufacture = device_info[0]['manufacturer']
            device_model = device_info[0]['model']
            device_model_id = device_info[0]['modelId']
            device_type = device_info[0]['formFactor']
            device_platform = device_info[0]['platform']
            device_os = device_info[0]['os']
            device_visibility = device_info[0]['fleetType']
            device_availability = device_info[0]['availability']

            print('Device Name - {} with Manufacture {}, model {}, modelId {} & type {}'.format(
                device_name,
                device_manufacture,
                device_model,
                device_model_id,
                device_type
            )
            )
            print('Device Platform {} with OS {}, visibility {} & availability - {} '.format(
                device_platform,
                device_os,
                device_visibility,
                device_availability
            )
            )

            if device_availability == TARGET_AVAILABILITY:
                print('AWS setup is complete')
            else:
                print('Problem, device is not available')
        else:
            print('Problem finding device info')

    except IndexError:
        print('Problem finding device from pool {}'.format(device_info))

if __name__ == '__main__':
    setup_aws_data()
