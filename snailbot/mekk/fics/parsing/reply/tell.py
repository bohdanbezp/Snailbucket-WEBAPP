# -*- coding: utf-8 -*-

"""
Parsing tell results.
"""

from mekk.fics.datatypes.generic import GenericText

import re
from mekk.fics import errors

# Use a raw string for the regular expression pattern
re_registered_required = re.compile(r'''
    ^Only\sregistered\susers\smay\ssend\stells
    ''', re.VERBOSE)

def parse_tell_reply(reply_text):
    """
    Parse result of telling sh.
    :param reply_text: FICS results
    """
    if re_registered_required.search(reply_text):
        raise errors.TrueAccountRequired(reply_text)
    # TODO: check for all positive cases (told XXX etc), throw or warn 
    # on the rest.
    return GenericText(reply_text.strip("\r\n "))
