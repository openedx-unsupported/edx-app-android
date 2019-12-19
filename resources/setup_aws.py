#!/usr/bin/env python

"""
This module will setup environment for AWS Device Farm
"""

import boto3
import requests

REGION = 'us-east-1' 
PROJECT_NAME = 'edx-app-test'
DEVICE_POOL_NAME = 'edx_devices_pool'

status_flag = False
device_farm = boto3.client('devicefarm', region_name=REGION)


def setup_aws_data():
    """
    setup data needed for AWS Device Farm
    """

    target_project_arn = setup_project(PROJECT_NAME)
    setup_device_pool(target_project_arn, DEVICE_POOL_NAME)

def setup_project(project_name):
    """
    Check if specific project exists, if not create new one by given name

    Arguments:
            project_arn (str): project arn

    Return:
            str: project arn value

    """

    project_arn = ''
    for project in device_farm.list_projects()['projects']:
        if project['name'] == project_name:
            print('{} already exists'.format(project_name))
            project_arn = project['arn']
        else:
            print('{} is not available, creating new one'.format(project_name))
            project_arn = create_project(project_name)

        return project_arn

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
    project_name = new_project['project']['name']
    print('{} created successfully.'.format(project_name))

    return new_project['project']['arn']

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
    for device_pool in device_farm.list_device_pools(arn=str(project_arn))['devicePools']:
        pool_name = device_pool['name']
        if pool_name == device_pool_name:
            print('{} already exists'.format(pool_name))
            print('{}'.format(device_pool))    
            target_device_pool_arn = device_pool['arn']
            is_device_pool_exists = True
            break
        else:
            is_device_pool_exists = False

    if not is_device_pool_exists:
        target_device_pool_arn = create_device_pool(
            device_pool_name, project_arn)

    return target_device_pool_arn

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
        maxDevices=10,
        rules=[
            {
                "attribute": "PLATFORM",
                "operator": "EQUALS",
                "value": '"ANDROID"'
            },
            {
                "attribute": "OS_VERSION",
                "operator": "EQUALS",
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

    new_pool_name = new_device_pool['devicePool']['name']
    new_pool_arn = new_device_pool['devicePool']['arn']
    print('{} is created successfully'.format(pool_name))
    return new_pool_arn

if __name__ == '__main__':
    setup_aws_data()
