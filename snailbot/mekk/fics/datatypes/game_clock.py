# -*- coding: utf-8 -*-

"""
Different small datatypes, used to encode information obtained from FICS
"""

class GameClock(object):
    """
    Representation of game clock info (some base + some increment). Object can be queried
    for different kinds of those attributes
    """
    __slots__ = ('_base', '_inc')

    def __init__(self, base_in_minutes, inc_in_seconds,
                 black_base_in_minutes=None, black_inc_in_seconds=None):
        self._base = base_in_minutes
        self._inc = inc_in_seconds
        if (black_base_in_minutes is not None) \
            and (black_inc_in_seconds is not None) \
            and (black_base_in_minutes != base_in_minutes
                 or black_inc_in_seconds != inc_in_seconds):
            # TODO: handle somehow different clocks case
            pass

    @property
    def base_min(self):
        """
        Returns base clock in minutes.
        :returns int
        """
        return self._base

    @property
    def base_sec(self):
        """
        Returns base clock in seconds.
        """
        return self._base * 60

    @property
    def inc_sec(self):
        """
        Returns increment in seconds.
        """
        return self._inc

    def time_for_moves_sec(self, moves_count):
        """
        Returns total time for given moves number (base clock + moves_count * increment)
        :param moves_count: how many moves (how many increments) to count
        :return: total time available for given moves count
        """
        return self.base_sec + moves_count * self.inc_sec

    @property
    def time_for_40_moves_sec(self):
        """
        Returns total time for 40 moves, in seconds (note that 300 is marker for blitz and
        900 is magic marker for standard game)
        """
        return self.time_for_moves_sec(40)

    @property
    def text_fics(self):
        """
        Formats clock info in FICS style (base in minutes, space, inc in seconds),
        for example "2 12" or "5 0"
        """
        return "%d %d" % (self._base, self._inc)

    @property
    def text(self):
        """
        Formats clock info in natural style (base in minutes, plus, inc in seconds),
        for example "2+12" or "5+0"
        """
        return "%d+%d" % (self._base, self._inc)

    def __str__(self):
        return self.text

    def __eq__(self, other):
        return (self._base == other._base) and (self._inc == other._inc)

    def __repr__(self):
        return "GameClock(%d+%d)" % (self._base, self._inc)

    def __cmp__(self, other):
        "Python2-only"
        return cmp(self._base, other._base) or cmp(self._inc, other._inc)

    def __lt__(self, other):
        return self._base < other._base or \
            (self._base == other._base and self._inc < other._inc)

    def __le__(self, other):
        return self._base < other._base or \
            (self._base == other._base and self._inc <= other._inc)

    def __gt__(self, other):
        return self._base > other._base or \
            (self._base == other._base and self._inc > other._inc)

    def __ge__(self, other):
        return self._base > other._base or \
            (self._base == other._base and self._inc >= other._inc)

    def __eq__(self, other):
        return self._base == other._base and self._inc == other._inc

    def __ne__(self, other):
        return self._base != other._base or self._inc != other._inc

    def __hash__(self):
        return hash(self._base * 100 + self._inc)

