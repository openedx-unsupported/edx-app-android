import json


def read_json_data(file_path):
    with open(file_path) as f:
        data = json.load(f)
    return data
