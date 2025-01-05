# -*- coding: utf-8 -*-

"""
Parsing date output.
"""

# pylint: disable=invalid-name

from mekk.fics import errors
from mekk.fics.datatypes.date import FicsDateInfo
import datetime
import re

# Local time     - Tue Jan  3, 23:47 EURCST 2012
# Server time    - Tue Jan  3, 13:47 PST 2012
# GMT            - Tue Jan  3, 21:47 GMT 2012

re_date_line = re.compile(r'''
(?P<which>Local\s+time|Server\s+time|GMT)
\s*-\s*
(?P<date>.*?)
\s+
(?P<zone>[A-Z]+)
\s+
(?P<year>[0-9]{4})
''', re.VERBOSE)

def parse_date_reply(text):
    """
    Parses date command reply, returns appropriate date information
    as the FicsDateInfo (all three dates as datetime.datetime
    objects plus their zone names)

    :param text: output of date command
    :return: dictionary with fields described above
    """
    #datetime.datetime.strptime("Tue Jan  3, 23:47 2012", "%a %b %d, %H:%M %Y")
    naming = {"GMT": "gmt", "Local time": "local", "Server time": "server"}
    dates = dict()
    # TODO: use parse_detailed_date
    for line in text.split("\n"):
        m = re_date_line.search(line)
        if m:
            zone = m.group('zone')
            name = naming[m.group('which')]
            date_obj = datetime.datetime.strptime(
                m.group("date") + " " + m.group("year"),
                "%a %b %d, %H:%M %Y")
            dates[name] = date_obj
            dates[name + "_zone_name"] = zone
    if ("local" in dates) and ("server" in dates) and ("gmt" in dates):
        return FicsDateInfo(**dates)     # pylint: disable=star-args
    else:
        raise errors.ReplyParsingException(text, "date")
