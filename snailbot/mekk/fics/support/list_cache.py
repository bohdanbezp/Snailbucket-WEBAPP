# -*- coding: utf-8 -*-

"""
Caching reply to queries like =computer
"""

from twisted.internet import reactor, defer
import logging
from mekk.fics.datatypes.player import PlayerName

logger = logging.getLogger("fics.lib")

class ListCache(object):
    """
    Cache FICS lists contents and reuse them for some time (used for things like TD or computers
    to avoid frequent checking).
    """
    def __init__(self, label, run_command):
        self.label = label
        self.run_command = run_command
        self._cache = dict()   # list name → cached values
        # Every item of _cache is dictionary with fields:
        #  creation_time - time in secs
        #  raw_list - bare list of items
        #  int_set - set of ints (if used)
        #  player_set - set of players (if used)

    @defer.inlineCallbacks
    def get_items(self, list_name, max_age_in_seconds,
                  treat_as_players=False, treat_as_ints=False):
        """
        Returns (as deferred) contents of given list, loading it if necessary but using
        cache if possible.

        If treat_as_players is set, returned object is set of PlayerName objects
        (ensuring case-less comparisons and easy belonging checking).

        If treat_as_ints is set, returned object is a set of int (useful for example
        for the list of subscribed channel).

        Otherwise plain list of strings is returned

        :param list_name: list to load (TD, computer, etc)
        :param max_age_in_seconds: if cache is older, it will be dropped and reloaded.
        :param treat_as_players: if set to true, returns result as set of PlayerName
        :param treat_as_ints: if set to true, returns result as set of ints
        :return: deferred fired with list of items (or set if one of treat*) is given
        """
        val = self._cache.get(list_name)
        if (val is None) or (val['creation_time'] + max_age_in_seconds > reactor.seconds()):
            logger.debug("%s: Loading list %s as it is unavailable or expired" % (self.label, list_name))
            when = reactor.seconds()
            info = yield self.run_command("= %s" % list_name)
            val = dict(creation_time=when, raw_list=info.items)
            self._cache[list_name] = val
        if treat_as_players:
            if not ('player_set' in val):
                val['player_set'] = set(PlayerName(item) for item in val['raw_list'])
            defer.returnValue(val['player_set'])
        elif treat_as_ints:
            if not ('int_set' in val):
                val['int_set'] = set(int(item) for item in val['raw_list'])
            defer.returnValue(val['int_set'])
        else:
            defer.returnValue(val['raw_list'])

