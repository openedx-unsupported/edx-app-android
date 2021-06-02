import os.path
from core import constants, log
import xml.etree.ElementTree as ET
import re


def validate(source_file, template):
    log.log_subline_bold(f"Reading source file : '{source_file}'.")
    source_file_string_placeholders = get_source_file_string_placeholders(
        source_file
    )
    source_file_plurals_placeholders = get_source_file_plurals_placeholders(
        source_file
    )
    for language in constants.languages.values():
        translation_file = template.replace('<lang>', language)
        if not os.path.exists(translation_file):
            log.log_warning(f"'{translation_file}' doesn't exist.")
            continue
        log.log_subline(f"Validating '{translation_file}'.")
        validate_string_translation(
            source_file_string_placeholders, translation_file
        )
        validate_plurals_translation(
             source_file_plurals_placeholders, translation_file
        )


def validate_string_translation(source_file_placeholders, translation_file):
    root = ET.parse(translation_file).getroot()
    for string in root.findall('string'):
        name = string.get('name')
        value = ''.join(string.itertext())
        if name in source_file_placeholders.keys():
            for placeholder in source_file_placeholders[name]:
                if not placeholder in value:
                    log.log_error(placeholder + " doesn't exist in '" +
                                  name + "'\n File: " + translation_file)


def validate_plurals_translation(source_file_placeholders, translation_file):
    for plural in ET.parse(translation_file).getroot().findall('plurals'):
        if plural.get('name') in source_file_placeholders:
            validate_item(plural, source_file_placeholders, translation_file)


def validate_item(plural, source_file_placeholders, translation_file):
    for item in plural.findall('item'):
        if item.get('quantity') in source_file_placeholders[plural.get('name')]:
            validate_quantity(plural, item, source_file_placeholders, translation_file)


def validate_quantity(plural, item, source_file_placeholders, translation_file):
    for placeholder in source_file_placeholders[plural.get('name')][item.get('quantity')]:
        if placeholder not in item.text:
            log.log_error(placeholder + " doesn't exist in item '" +
                          item.get('quantity') + "' of plural '" + plural.get('name') +
                          "'\n File: " + translation_file)


def get_source_file_string_placeholders(file):
    """Reads the source xml file and return a dictionary having string name as
    key and tuple of existing placeholders as values"""
    return {
        string.get("name"): get_placeholders("".join(string.itertext()))
        for string in ET.parse(file).getroot().findall("string")
        if get_placeholders("".join(string.itertext()))
    }


def get_source_file_plurals_placeholders(file):
    placeholders = {}
    root = ET.parse(file).getroot()
    for plural in root.findall('plurals'):
        placeholders.update(get_plural_items_placeholders(plural))
    return placeholders


def get_plural_items_placeholders(plural): #new
    placeholders = {
        plural.get("name"): {
            item.get("quantity"): get_placeholders(item.text)
            for item in plural.findall("item")
            if get_placeholders(item.text)
        }
    }
    return {_: item for _, item in placeholders.items() if item}


def get_placeholders(str):
    return re.findall("{?[a-zA-Z0-9_]+}", str)
