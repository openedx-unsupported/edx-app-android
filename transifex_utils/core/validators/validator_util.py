from core import constants, log, json_util


def match_particular_property(index, source_data_field, translation_data_field,
                              translation_file):
    """Logs error if particular 'source_data_field' doesn't match with the
        particular 'translation_data_field'

        Parameters:
            'index': json main list index
            'source_data_field': will refer to a particular source data field object
            'translation_data_field': will refer to a particular translation data field object
            'translation_file': translation file whose data is getting compared
    """
    if source_data_field != translation_data_field:
        log.log_error(f"Keys don't matched." +
                      f"\nValue: '{translation_data_field}'" +
                      f"\nExpected Value: '{source_data_field}'" +
                      f"\nList Index: {index}"
                      f"\nFile: '{translation_file}'"
                      )


def match_property(index, property,
                   source_data, translation_data, translation_file):
    """Logs error if 'source_data' property doesn't match with the
        'translation_data' property.

        Parameters:
            'index': json main list index
            'property': property which needs to be compared
            'source_data': will refer to a source json data object
            'translation_data': will refer to a translation json data object
            'translation_file': translation file which whose data is getting compared
    """
    if source_data[index][property] != translation_data[index][property]:
        log.log_error(f"Keys don't matched." +
                      f"\nValue: '{translation_data[index][property]}'" +
                      f"\nExpected Value: '{source_data[index][property]}'" +
                      f"\nProperty: '{property}'"
                      f"\nList Index: {index}"
                      f"\nFile: '{translation_file}'"
                      )
