import sys

from core import constants


class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'


def log_header(line):
    print(bcolors.HEADER + "==> " + line + bcolors.ENDC)


def log_line(line):
    print(bcolors.BOLD + "==> " + line + bcolors.ENDC)


def log_subline(line):
    print(" -> " + line)


def log_subline_bold(line):
    print(bcolors.BOLD + " -> " + line + bcolors.ENDC)


def log_error(line):
    print(bcolors.FAIL + "--- ERROR --- : " + line + bcolors.ENDC)
    if constants.HALT_SCRIPT_ON_ERROR:
        sys.exit()


def log_warning(line):
    print(bcolors.WARNING + "--- WARNING --- : " + line + bcolors.ENDC)
