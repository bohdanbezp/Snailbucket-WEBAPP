# -*- coding: utf-8 -*-

"""
FICS time
"""

from collections import namedtuple

#noinspection PyClassicStyleClass
class FicsDateInfo (
        namedtuple(
            "FicsDateInfo",
            "local, local_zone_name, gmt, gmt_zone_name, server, server_zone_name")):
    """
    Aggregate representation of complete FICS current time information. Contains
    the following fields:

    - local - datetime in local timezone (whatever is set in the bot/client config)
    - server - datetime in server timezone
    - gmt - datetime in gmt
    - local_zone_name - local timezone name
    - server_zone_name - server timezone name ("PST" - until it is reconfigured)
    - gmt_zone_name - GMT timezone name ("GMT")

    The local, server and gmt fields are datetime.datetime objects
    without timezone information (reasoning: timezone support in Python is
    clumsy, moreover, not all FICS timezones are known to Python parser).
    """
    pass

