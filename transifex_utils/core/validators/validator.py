from core import constants, log
from core.validators import (subjects_json_validator, profiles_json_validator,
                             whats_new_json_validator, strings_validator)


def validate_all_translations():
    log.log_line("Starting translations validation.")

    for template in constants.files_templates:
        source_file = template.replace("-<lang>", "")
        if source_file.endswith(".xml"):
            strings_validator.validate(source_file, template)
        elif source_file.endswith(constants.PROFILES_JSON):
            profiles_json_validator.validate(source_file, template)
        elif source_file.endswith(constants.SUBJECTS_JSON):
            subjects_json_validator.validate(source_file, template)
        elif source_file.endswith(constants.WHATS_NEW_JSON):
            whats_new_json_validator.validate(source_file, template)

    log.log_line("Translations validation finished successfully.")
