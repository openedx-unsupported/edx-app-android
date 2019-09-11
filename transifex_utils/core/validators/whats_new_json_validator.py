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
        validate_translation_data(source_data, translation_data,
                                  translation_file)


def validate_translation_data(source_data, translation_data,
                              translation_file):
    for i in range(0, len(translation_data)):
        validate_fields(i, source_data, translation_data, translation_file)


def validate_fields(i, source_data, translation_data, translation_file):
    # version
    validator_util.match_property(i, "version", source_data, translation_data,
                                  translation_file)
    for mi in range(0, len(source_data[i]["messages"])):
        # [i]["messages"][mi]["image"]
        validator_util.match_particular_property(i,
                                                 source_data[i]
                                                 ["messages"][mi]["image"],
                                                 translation_data[i]
                                                 ["messages"][mi]["image"],
                                                 translation_file)
        for pi in range(0, len(source_data[i]["messages"][mi]["platforms"])):
            # [i]["messages"][mi]["platforms"][pi]
            validator_util.match_particular_property(i,
                                                     source_data[i]
                                                     ["messages"][mi]
                                                     ["platforms"][pi],
                                                     translation_data[i]
                                                     ["messages"][mi]
                                                     ["platforms"][pi],
                                                     translation_file)
