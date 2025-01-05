# -*- coding: utf-8 -*-

"""
Parsing seek info.
"""

import re
from mekk.fics.datatypes.notifications import SeekRef, Seek
#from mekk.fics.datatypes.generic import UnknownReply
from mekk.fics.datatypes.game_clock import GameClock
from mekk.fics.datatypes.player import PlayerName
from mekk.fics.datatypes.game_info import GameSpec
from mekk.fics.datatypes.game_type import GameType
from mekk.fics.datatypes.color import Color
from mekk.fics.parsing.common import rated_as_bool, numeric_rank
from mekk.fics import errors

import logging
log = logging.getLogger("fics")

re_proper_seeks = [
    re.compile(regexp)
    for regexp in [
       '^Your seek has been posted with index (?P<seek_no>\d+)\.',
       '^Updating seek ad (?P<seek_no>\d+);',
      ]]

# TODO: więcej
re_bad_seeks = re.compile(
    '''^No such board'''
    )

def parse_seek_reply(reply_text):
    """
    Parse output of "seek" command
    :param reply_text: actual FICS output
    :type reply_text: str
    :return: seek details (or None)
    :rtype: SeekRef
    :raise: errors.BadFicsCommandParameters
    """
    for good_re in re_proper_seeks:
        m = good_re.search(reply_text)
        if m:
            return SeekRef(seek_no = int(m.group('seek_no')))
    if re_bad_seeks.search(reply_text):
        raise errors.BadFicsCommandParameters(reply_text)
    # Sygnał że nie rozumiemy
    return None

re_sought_line = re.compile('^ *(?P<seek_no>\d+) +(?P<rank>\d+|\+\+\+\+|----) +(?P<who>[^\s()]+)(?:\(\S+\))* +(?P<base>\d+) +(?P<inc>\d+) +(?P<rated>rated|unrated) +(?P<variant>\S+) *?(?:\[(?P<color>white|black)\])? *(?P<min_rating>\d+)-(?P<max_rating>\d+) *(?P<flags>[mf]*)')
re_sought_finish = re.compile('^\d+ ads? displayed')

def parse_sought_reply(reply_text):
    lines = reply_text.strip(" \r\n").split("\n")
    if lines and re_sought_finish.search(lines[-1]):
        seeks = []
        for line in lines[:-1]:
            m = re_sought_line.search(line)
            if not m:
                log.warn("Can't parse sought line: %s" % line)
                continue
            seeks.append(
                Seek(seek_no=int(m.group('seek_no')),
                     player=PlayerName(m.group('who')),
                     player_rating_value=numeric_rank(m.group('rank')), 
                     is_manual=('m' in m.group('flags')),
                     using_formula=('f' in m.group('flags')),
                     color=(m.group('color') and Color(m.group('color')) or None),
                     game_spec=GameSpec(
                        game_type=GameType(m.group('variant')),
                        clock=GameClock(int(m.group('base')), int(m.group('inc'))),
                        is_rated=rated_as_bool(m.group('rated')),
                        is_private='p' in m.group('flags')), # TODO: czy to dobrze
                ))
        return seeks
    return None
