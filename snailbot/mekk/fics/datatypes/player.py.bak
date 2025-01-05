# -*- coding: utf-8 -*-

"""
Tricky names.
"""
from __future__ import unicode_literals
import six
from collections import namedtuple

# How much of name remains in truncated form
TRUNCATED_LEN = 8 # TODO: check, it is probably a bit more. But never set it too high!

class PlayerName(object):
    """
    Representation of a player handle. The class mostly behaves
    as a string, but all comparison operations are case agnostic
    (i.e. MekK == mekk) to mimic FICS behaviour.

    Also makes comparison equal for possibly truncated names
    """
    __slots__ = 'name', 'trunc'

    def __init__(self, name, can_be_truncated=False):
        if isinstance(name, PlayerName):
            name = name.name
        self.name = name
        # If name is short, it can't be truncated
        self.trunc = can_be_truncated and len(name) >= TRUNCATED_LEN

    @property
    def lowername(self):
        return self.name.lower()

    def __str__(self):
        return self.name

    def __repr__(self):
        return "PlayerName(%s)" % self.name

    def __eq__(self, other):
        if isinstance(other, six.string_types):
            other_name = other.lower()
            other_trunc = False
        else:
            assert isinstance(other, PlayerName)
            other_name = other.name.lower()
            other_trunc = other.trunc
        my_name = self.name.lower()
        if my_name == other_name:
            return True
        if self.trunc and not other_trunc and other_name.startswith(my_name):
            return True
        if not self.trunc and other_trunc and my_name.startswith(other_name):
            return True
        return False

    def __cmp__(self, other):
        if isinstance(other, six.string_types):
            other_name = other.lower()
            other_trunc = False
        else:
            other_name = other.name.lower()
            other_trunc = other.trunc
        my_name = self.name.lower()
        if my_name == other_name:
            return 0
        if self.trunc and not other_trunc and other_name.startswith(my_name):
            return 0
        if not self.trunc and other_trunc and my_name.startswith(other_name):
            return 0
        return cmp(my_name, other_name)

    def __hash__(self):
        # To stay equal to other possible shortened items
        return hash(self.name.lower()[:TRUNCATED_LEN])

# TODO: glue for calculating full handle using "handles prefix" command

#noinspection PyClassicStyleClass
class PlayerRating(namedtuple("PlayerRating",
    "value, rd")):
    """
    Representation of player rating. Fields:

    - value (int) - actual rating (value 0 means no rating - happens in case of guest
        and players who have not played any game)
    - rd (decimal.Decimal) - rating deviation
    """
    #noinspection PyUnresolvedReferences
    def __str__(self):
        if self.value:
            return "%d(%s)" % (self.value, str(self.rd))
        else:
            return "----"

    def is_known(self):
        return self.value and True or False

#noinspection PyClassicStyleClass
class ResultStats(namedtuple("ResultStats",
    "rating,wins_count,draws_count,losses_count,best")):
    """
    Info about player's results in some game. Fields:

    - rating (PlayerRating) - current rating
    - wins_count (int)
    - draws_count (int)
    - losses_count (int)
    - best (int) - best rating (just int, without rd), can be None
    """
    pass

#noinspection PyClassicStyleClass
class FingerInfo(namedtuple("FingerInfo",
    "name,results,plan")):
    """
    Player's finger. Fields:

    - name (PlayerName) - nick
    - results (dictionary mapping GameType to ResultStats) - rating
        and result statistics for all games player happened to play
    - plan (array of up to 10 strings) - finger notes text
    """
    pass


class IdleInfo(namedtuple("IdleInfo",
    "name, idle")):
    """
    Player annotated with idling information.

    - name (PlayerName) - nick
    - idle - how many *seconds* the player was idle (or 0 if is active) 
    """
    pass


class ZnotifyInfo(namedtuple("ZnotifyInfo",
    "tracked, tracking")):
    """
    Aggregated information about present players on notify list,
    and players having «me» on notify list (results of znotify command).

    - tracked: players on my notify list
    - tracking: players who have me on notify list (and who are not tracked)

    Both tracked and tracking are lists of IdleInfo
    """
    pass
