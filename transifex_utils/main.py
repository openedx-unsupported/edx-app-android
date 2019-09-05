from core.validators import validator
from core import download_translations, log

log.log_header("*******************************")
log.log_header("*** Script running started. ***")
log.log_header("*******************************")

download_translations.download()

validator.validate_all_translations()

log.log_header("*** Done. ***")
