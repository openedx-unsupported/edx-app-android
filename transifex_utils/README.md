# edX Android Transifex Util

This directory contains the utility python3 script.
 
Project is developed to automate:
1. The process of downloading all the translation assets of supported 
languages from transifex using 
[transifex client](https://docs.transifex.com/client/introduction) . 
2. Validate all the downloaded translation assets and make sure the
   placeholders/keys of translation files are matching with source files 
   i-e- english files.


# Procedure:
1. Review the following 2 files to check the supported languages and
   files and make sure they are synced.
   - `transifex_utils/core/constants.py`
   - `edx-app-android/.tx/config`
2. Open the terminal/command prompt and change directory to project root.
3. Run the following command.
 
        python3 transifex_utils/main.py
