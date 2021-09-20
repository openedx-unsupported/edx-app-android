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
    items = plural.findall('item')
    source_placeholder = list(source_file_placeholders[plural.get('name')].values())[0]

    for item in items:
        if (get_placeholders(item.text) != source_placeholder):
            log.log_error("Plural '" + plural.get('name') + "': item '" + 
            item.get('quantity') + "' contain different placeholder " + 
            "or format specifier from default string \n File: " + translation_file)

    for item in items:
        if item.get('quantity') in source_file_placeholders[plural.get('name')]:
            validate_quantity(plural, item, source_file_placeholders, translation_file)


def validate_quantity(plural, item, source_file_placeholders, translation_file):
    plural_name = plural.get('name')
    quantity = item.get('quantity')
    for placeholder in source_file_placeholders[plural_name][quantity]:
        if placeholder not in item.text:
            log.log_error(placeholder + " doesn't exist in item '" +
                          quantity + "' of plural '" + plural_name +
                          "'\n File: " + translation_file)


def get_source_file_string_placeholders(file):
    """Reads the source xml file and return a dictionary having string name as
    key and tuple of existing placeholders as values"""
    placeholders = {}
    root = ET.parse(file).getroot()
    for element in root.findall('string'):
        name = element.get('name')
        value = ''.join(element.itertext())
        placeholder = get_placeholders(value)
        if placeholder:
            placeholders[name] = placeholder
    return placeholders


def get_source_file_plurals_placeholders(file):
    placeholders = {}
    root = ET.parse(file).getroot()
    for plural in root.findall('plurals'):
        placeholders.update(get_plural_items_placeholders(plural))
    return placeholders


def get_plural_items_placeholders(plural): #new
    plural_name = plural.get('name')
    placeholders = {
        plural_name: {
            item.get("quantity"): get_placeholders(item.text)
            for item in plural.findall("item")
            if get_placeholders(item.text)
        }
    }
    return placeholders if placeholders[plural_name] else {}


def get_placeholders(str):
    return re.findall("{?[a-zA-Z0-9_]+}|%d|%f|%s", str)
