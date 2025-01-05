# -*- coding: utf-8 -*-

"""
Game variants.
"""

import six

# Game shortcut markers used in games command
GAMES_GAMETYPE_CODES = (
    ('b', 'blitz'),
    ('B', 'bughouse'),
    ('l', 'lightning'),
    ('L', 'losers'),
    ('s', 'standard'),
    ('S', 'suicide'),
    ('w', 'wild'),
    ('x', 'atomic'),
    ('z', 'crazyhouse'),
    ('n', 'nonstandard'), # For example different time controls for white than for black.
    ('u', 'untimed'),  # Also simuls
    )

GAMES_GAMETYPE_SHORT_TO_LONG = dict(GAMES_GAMETYPE_CODES)

class GameType(object):
    """
    Representation of game type (standard, blitz, atomic, ...). This class helps
    to construct canonical representation from different notations which happen on FICS.
    """
    __slots__ = '_game_type',

    def __init__(self, game_type):
        """
        Constructs the object. Handles different formats of game_type:

        - full canonical name (like 'standard', or 'wild/fr'),
        - one-letter shortcut seen in "games" (like 'b' or 'S'),
        - capitalized name seen in finger (like "Standard", "Atomic")

        :param game_type: string game type description, as described above.
        """
        assert isinstance(game_type, six.string_types)
        if len(game_type) == 1:
            self._game_type = GAMES_GAMETYPE_SHORT_TO_LONG[game_type]
        else:
            self._game_type = game_type.lower()

    @property
    def name(self):
        """
        Returns full game name. Note, that while for most games it does not matter
        how the object was created, in case of wild games reply vary - may be generic
        'wild' if more specific information is not available, or 'wild/fr', 'wild/3' etc
        if it is known.

        :return: Appropriate lowercase name ('standard', 'atomic', 'blitz', ...)
        """
        return self._game_type

    def __str__(self):
        return self.name

    def __eq__(self, other):
        if isinstance(other, str):
            return self._game_type == other
        return self._game_type == other._game_type

    def matches(self, other):
        """
        Weaker comparison, returns True when wild is compred with wild/fr and in similar
        cases
        """
        return self._game_type == other._game_type \
            or self._game_type.startswith(other._game_type + "/") \
            or other._game_type.startswith(self._game_type + "/")

    def __repr__(self):
        return "GameType(%s)" % self._game_type

    def __cmp__(self, other):
        return cmp(self._game_type, other._game_type)

    def __lt__(self, other):
        return self._game_type < other._game_type

    def __le__(self, other):
        return self._game_type <= other._game_type

    def __gt__(self, other):
        return self._game_type > other._game_type

    def __ge__(self, other):
        return self._game_type >= other._game_type

    def __eq__(self, other):
        return self._game_type == other._game_type

    def __ne__(self, other):
        return self._game_type != other._game_type


    def __hash__(self):
        return hash(self._game_type)

# TODO: is wild vs wild/fr any problem here?
