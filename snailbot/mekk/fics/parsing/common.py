# -*- coding: utf-8 -*-

"""
Reusable helper methods.
"""

import datetime
from mekk.fics import errors

import re

def rated_as_bool(text):
    """
    Returns true if param = "rated" and false otherwise
    (usually if param = "unrated"). Used inside different parsing context.
    :param text: "rated" or "unrated"
    """
    if text == 'rated':
        return True
    else:
        return False

re_nrank=re.compile("(\d+)[PE]?")
def numeric_rank(text):
    """
    Converts textual rank description to the number.
    Strips suffixes like "P" or "E", for unknown ranks
    (like ----) returns 0.
    :param text: text like "1960", "1960P", "1960PE", "----"
    """
    m = re_nrank.match(text)
    if m:
        return int(m.group(1))
    return 0

# Sat Sep 15, 23:34 EURCST 2012  itp
re_date_with_zone = re.compile(r'''
(?P<date>[A-Z][a-z]+\s+[A-Z][a-z]+\s+\d+,\s*\d+:\d+)
\s+
(?P<zone>[A-Z]+)
\s+
(?P<year>[0-9]{4})
''', re.VERBOSE)

def parse_detailed_date(text):
    """
    Parses detailed date text. Returns pair: (tz-naive datetime, timezone name)
    Note: timezone names are non-standard (FICS configuration), for example EURCST

    :param text: output of date command
    :return: datetime, zone-name
    """
    m = re_date_with_zone.search(text)
    if m:
        zone = m.group('zone')
        date_obj = datetime.datetime.strptime(
            m.group("date") + " " + m.group("year"),
            "%a %b %d, %H:%M %Y")
        return date_obj, zone
    raise errors.ReplyParsingException(
        "Can not parse date: '%s'" % text, "date")