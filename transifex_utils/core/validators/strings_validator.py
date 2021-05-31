import os.path
from core import constants, log
import xml.etree.ElementTree as ET
import re


def validate(source_file, template):
    log.log_subline_bold(f"Reading source file : '{source_file}'.")
    source_file_string_placeholders_dict = get_source_file_string_placeholders_dict(
        source_file
    )
    source_file_plurals_placeholders_dict = get_source_file_plurals_placeholders_dict(
        source_file
    )
    for language in constants.languages.values():
        translation_file = template.replace('<lang>', language)
        if not os.path.exists(translation_file):
            log.log_warning(f"'{translation_file}' doesn't exist.")
            continue
        log.log_subline(f"Validating '{translation_file}'.")
        validate_string_translation(
            source_file_string_placeholders_dict, translation_file
        )
        validate_plurals_translation(
            source_file_plurals_placeholders_dict, translation_file
        )


def validate_string_translation(source_file_placeholders_dict, translation_file):
    root = ET.parse(translation_file).getroot()
    for str_element in root.findall('string'):
        str_name = str_element.get('name')
        str_value = ''.join(str_element.itertext())
        if str_name in source_file_placeholders_dict.keys():
            for placeholder in source_file_placeholders_dict[str_name]:
                if not placeholder in str_value:
                    log.log_error(placeholder + " doesn't exist in '" +
                                  str_name + "'\n File: " + translation_file)


def validate_plurals_translation(source_file_placeholders_dict, translation_file):
    root = ET.parse(translation_file).getroot()
    for plural_element in root.findall('plurals'):
        plural_name = plural_element.get('name')
        if plural_name not in source_file_placeholders_dict.keys():
            continue
        for plural_item in plural_element.findall('item'):
            item_quantity = plural_item.get('quantity')
            item_value = plural_item.text
            if item_quantity not in source_file_placeholders_dict[plural_name].keys():
                continue
            for placeholder in source_file_placeholders_dict[plural_name][item_quantity]:
                if not placeholder in item_value:
                    log.log_error(placeholder + " doesn't exist in item '" +
                                  item_quantity + "' of plural '" + plural_name +
                                  "'\n File: " + translation_file)


def get_source_file_string_placeholders_dict(file):
    """Reads the source xml file and return a dictionary having string name as
    key and tuple of existing placeholders as values"""
    placeholders_dict = {}
    root = ET.parse(file).getroot()
    for str_element in root.findall('string'):
        str_name = str_element.get('name')
        str_value = ''.join(str_element.itertext())
        placeholders = get_placeholders(str_value)
        if placeholders:
            placeholders_dict[str_name] = placeholders
    return placeholders_dict


def get_source_file_plurals_placeholders_dict(file):
    placeholders_dict = {}
    root = ET.parse(file).getroot()
    for plural_element in root.findall('plurals'):
        placeholders_dict.update(get_plural_items_placeholders(plural_element))
    return placeholders_dict


def get_plural_items_placeholders(plural_element):
    placeholders_dict = {}
    name = plural_element.get('name')
    for item in plural_element.findall('item'):
        item_quantity = item.get('quantity')
        item_value = item.text
        item_placeholder = get_placeholders(item_value)
        if item_placeholder:
            placeholders_dict[name] = {item_quantity: item_placeholder}
    return placeholders_dict

def get_placeholders(str):
    return re.findall("{?[a-zA-Z0-9_]+}", str)
