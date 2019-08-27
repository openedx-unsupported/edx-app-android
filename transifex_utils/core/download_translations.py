import os.path
from core import constants, log


def get_language_codes():
    # Return a string having comma separated supported language names
    return ",".join(list(constants.languages.keys()))


def download():
    # Download all the translation assets from transifex via transifex client
    language_codes = get_language_codes()
    log.log_line(f"Downloading translations for {language_codes}")
    os.system("tx pull -f -l " + language_codes)
    log.log_line("Translations downloading finished.")
