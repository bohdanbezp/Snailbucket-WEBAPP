# -*- coding: utf-8 -*-

"""
Those are strictly internal for mekk.fics own test suite.

Kept here to free me from inventing separate module path for those utilities

Not intended to be used anywhere outside mekk.fics tests
"""

import re
from .helpers import load_tstdata_file


FICS_PARSE_DATA_DIR = "ficsparserdata"

def load_parse_data_file(name):
    return load_tstdata_file(FICS_PARSE_DATA_DIR, name)

def load_parse_data_file_patching_continuations(name):
    data = load_parse_data_file(name)
    # Usually \-lines are joined by BlockFilter or disabled by nowrap.
    # In tests where we don't use any of those, but utilize console grabs,
    # it is necessary to manually patch the data in the same way.
    return re.sub(r'\s?\n\\ *', ' ', data)
