import os.path
from core import constants, log
import xml.etree.ElementTree as ET
import re


def validate(source_file, template):
    log.log_subline_bold(f"Reading source file : '{source_file}'.")
    source_file_placeholders_dict = get_source_file_placeholders_dict(
        source_file)
    for language in constants.languages.values():
        translation_file = template.replace('<lang>', language)
        if not os.path.exists(translation_file):
            log.log_warning(f"'{translation_file}' doesn't exist.")
            continue
        log.log_subline(f"Validating '{translation_file}'.")
        validate_translation(source_file_placeholders_dict, translation_file)


def validate_translation(source_file_placeholders_dict, translation_file):
    root = ET.parse(translation_file).getroot()
    for str_element in root.findall('string'):
        str_name = str_element.get('name')
        str_value = str_element.text
        if str_name in source_file_placeholders_dict.keys():
            for placeholder in source_file_placeholders_dict[str_name]:
                if not placeholder in str_value:
                    log.log_error(placeholder + " doesn't exist in '" +
                                  str_name + "'\n File: " + translation_file)


def get_source_file_placeholders_dict(file):
    """Reads the source xml file and return a dictionary having string name as
    key and tuple of existing placeholders as values"""
    dict = {}
    root = ET.parse(file).getroot()
    for str_element in root.findall('string'):
        str_name = str_element.get('name')
        str_value = str_element.text
        placeholders = get_placeholders(str_value)
        if len(placeholders) > 0:
            dict[str_name] = placeholders
    return dict


def get_placeholders(str):
    return re.findall("{?[a-zA-Z0-9_]+}", str)
