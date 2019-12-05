import os.path
from core import constants, log
import xml.etree.ElementTree as ET
import re


def validate(source_file, template):
    log.log_subline_bold(f"Reading source file : '{source_file}'.")

    source_file_placeholders_dict_for_string_tags = get_source_file_placeholders_dict_for_string_tags(
        source_file)
    source_file_placeholders_dict_for_plurals_tags = get_source_file_placeholders_dict_for_plurals_tags(
        source_file)

    for language in constants.languages.values():
        translation_file = template.replace('<lang>', language)
        if not os.path.exists(translation_file):
            log.log_warning(f"'{translation_file}' doesn't exist.")
            continue
        log.log_subline(f"Validating '{translation_file}'.")
        validate_translation_string_tags(
            source_file_placeholders_dict_for_string_tags, translation_file)
        validate_translation_plurals_tags(
            source_file_placeholders_dict_for_plurals_tags, translation_file)


def validate_translation_string_tags(source_file_placeholders_dict,
                                     translation_file):
    root = ET.parse(translation_file).getroot()
    for str_element in root.findall('string'):
        str_name = str_element.get('name')
        str_value = str_element.text
        if str_name in source_file_placeholders_dict.keys():
            for placeholder in source_file_placeholders_dict[str_name]:
                if not placeholder in str_value:
                    log.log_error(
                        f"{placeholder} doesn't exist in '{str_name}'\n File: {translation_file}")


def validate_translation_plurals_tags(source_file_placeholders_dict,
                                      translation_file):
    root = ET.parse(translation_file).getroot()
    for str_element in root.findall('plurals'):
        str_name = str_element.get('name')
        if str_name in source_file_placeholders_dict.keys():
            for item in str_element.findall('item'):
                item_quantity = item.get('quantity')
                for placeholder in source_file_placeholders_dict[str_name]:
                    if not placeholder in item.text:
                        log.log_error(
                            f"{placeholder} doesn't exist in '{str_name}'->'{item_quantity}'\n File: {translation_file}")
        else:
            for item in str_element.findall('item'):
                item_quantity = item.get('quantity')
                if not "%s" in item.text:
                    log.log_error(
                        f"'%s' might be missing in '{str_name}'->'{item_quantity}'\n File: {translation_file}")


def get_source_file_placeholders_dict_for_plurals_tags(file):
    """Reads the string tags from source xml file and return a dictionary having
    string name as key and tuple of existing placeholders as values"""
    diction = {}
    root = ET.parse(file).getroot()
    for str_element in root.findall('plurals'):
        str_name = str_element.get('name')
        for item in str_element.findall('item'):
            item_quantity = item.get('quantity')
            str_value = item.text
            placeholders = get_placeholders(str_value)
            if len(placeholders) > 0:
                diction[str_name] = placeholders
                continue
    return diction


def get_source_file_placeholders_dict_for_string_tags(file):
    """Reads the string tags from source xml file and return a dictionary having
    string name as key and tuple of existing placeholders as values"""
    diction = {}
    root = ET.parse(file).getroot()
    for str_element in root.findall('string'):
        str_name = str_element.get('name')
        str_value = str_element.text
        placeholders = get_placeholders(str_value)
        if len(placeholders) > 0:
            diction[str_name] = placeholders
    return diction


def get_placeholders(str):
    return re.findall("{?[a-zA-Z0-9_]+}", str)
