import os.path
import json
from core import constants, log, json_util
from core.validators import validator_util


def validate(file_path, template):
    log.log_subline_bold(f"Reading source file : '{file_path}'.")
    source_data = json_util.read_json_data(file_path)
    for language in constants.languages.values():
        translation_file = template.replace('<lang>', language)
        if not os.path.exists(translation_file):
            log.log_warning(f"'{translation_file}' doesn't exist.")
            continue
        log.log_subline(f"Validating '{translation_file}'.")
        translation_data = json_util.read_json_data(translation_file)
        validate_translation_data(source_data["fields"],
                                  translation_data["fields"],
                                  translation_file)


def validate_translation_data(source_data, translation_data, translation_file):
    validate_field_0(source_data, translation_data, translation_file)
    validate_field_1(source_data, translation_data, translation_file)
    validate_field_2(source_data, translation_data, translation_file)
    validate_field_3(source_data, translation_data, translation_file)
    validate_field_4(source_data, translation_data, translation_file)


def validate_field_0(source_data, translation_data, translation_file):
    # index
    i = 0
    # name
    validator_util.match_property(i, "name", source_data, translation_data,
                                  translation_file)
    # type
    validator_util.match_property(i, "type", source_data, translation_data,
                                  translation_file)
    # data_type
    validator_util.match_property(i, "data_type", source_data,
                                  translation_data,
                                  translation_file)
    # ["options"]["values"][0]["value"]
    validator_util.match_particular_property(i,
                                             source_data[i]["options"]
                                             ["values"][0]["value"],
                                             translation_data[i]["options"]
                                             ["values"][0]["value"],
                                             translation_file)
    # ["options"]["values"][1]["value"]
    validator_util.match_particular_property(i,
                                             source_data[i]["options"]
                                             ["values"][1]["value"],
                                             translation_data[i]["options"]
                                             ["values"][1]
                                             ["value"],
                                             translation_file)


def validate_field_1(source_data, translation_data, translation_file):
    # index
    i = 1
    # name
    validator_util.match_property(i, "name", source_data, translation_data,
                                  translation_file)
    # type
    validator_util.match_property(i, "type", source_data, translation_data,
                                  translation_file)
    # ["options"]["range_min"]
    validator_util.match_particular_property(i,
                                             source_data[i]["options"]
                                             ["range_min"],
                                             translation_data[i]["options"]
                                             ["range_min"],
                                             translation_file)
    # ["options"]["range_max"]
    validator_util.match_particular_property(i,
                                             source_data[i]["options"]
                                             ["range_max"],
                                             translation_data[i]["options"]
                                             ["range_max"],
                                             translation_file)
    # ["options"]["allows_none"]
    validator_util.match_particular_property(i,
                                             source_data[i]["options"]
                                             ["allows_none"],
                                             translation_data[i]["options"]
                                             ["allows_none"],
                                             translation_file)


def validate_field_2(source_data, translation_data, translation_file):
    # index
    i = 2
    # name
    validator_util.match_property(i, "name", source_data, translation_data,
                                  translation_file)
    # type
    validator_util.match_property(i, "type", source_data, translation_data,
                                  translation_file)
    # data_type
    validator_util.match_property(i, "data_type", source_data,
                                  translation_data, translation_file)
    # ["options"]["reference"]
    validator_util.match_particular_property(i,
                                             source_data[i]["options"]
                                             ["reference"],
                                             translation_data[i]["options"]
                                             ["reference"],
                                             translation_file)
    # ["options"]["allows_none"]
    validator_util.match_particular_property(i,
                                             source_data[i]["options"]
                                             ["allows_none"],
                                             translation_data[i]["options"]
                                             ["allows_none"],
                                             translation_file)


def validate_field_3(source_data, translation_data, translation_file):
    # index
    i = 3
    # name
    validator_util.match_property(i, "name", source_data, translation_data,
                                  translation_file)
    # type
    validator_util.match_property(i, "type", source_data, translation_data,
                                  translation_file)
    # data_type
    validator_util.match_property(i, "data_type", source_data,
                                  translation_data, translation_file)
    # ["options"]["reference"]
    validator_util.match_particular_property(i,
                                             source_data[i]["options"]
                                             ["reference"],
                                             translation_data[i]["options"]
                                             ["reference"],
                                             translation_file)
    # ["options"]["allows_none"]
    validator_util.match_particular_property(i,
                                             source_data[i]["options"]
                                             ["allows_none"],
                                             translation_data[i]["options"]
                                             ["allows_none"],
                                             translation_file)


def validate_field_4(source_data, translation_data, translation_file):
    # index
    i = 4
    # name
    validator_util.match_property(i, "name", source_data, translation_data,
                                  translation_file)
    # type
    validator_util.match_property(i, "type", source_data, translation_data,
                                  translation_file)
