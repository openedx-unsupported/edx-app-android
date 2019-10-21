"""Halt the script on some error"""
HALT_SCRIPT_ON_ERROR = False

"""Dictionary having data of supported languages.
   Keys belong to transifex language codes while
   Values belong to android supported language codes."""
languages = {
    'es_419': 'es',
    'tr_TR': 'tr',
    'zh_CN': 'zh',
    'pt_BR': 'pt-rBR',
    'fr': 'fr',
    'de_DE': 'de-rDE',
    'he': 'iw',
}

SUBJECTS_JSON = "subjects.json"
PROFILES_JSON = "profiles.json"
COUNTRIES_JSON = "countries.json"
LANGUAGES_JSON = "languages.json"
WHATS_NEW_JSON = "whats_new.json"

"""List of all the supported transifex file templates."""
files_templates = {
    "OpenEdXMobile/res/values-<lang>/strings.xml",
    "OpenEdXMobile/res/values-<lang>/errors.xml",
    "OpenEdXMobile/res/values-<lang>/labels.xml",
    "OpenEdXMobile/res/raw-<lang>/" + PROFILES_JSON,
    "OpenEdXMobile/res/raw-<lang>/" + COUNTRIES_JSON,
    "OpenEdXMobile/res/raw-<lang>/" + LANGUAGES_JSON,
    "OpenEdXMobile/res/raw-<lang>/" + SUBJECTS_JSON,
    "OpenEdXMobile/res/raw-<lang>/" + WHATS_NEW_JSON,
}
