# -*- coding: utf-8 -*-

"""
Parsing gin.
"""

# pylint: disable=invalid-name,line-too-long

from mekk.fics import errors
from mekk.fics.datatypes.game_clock import GameClock
from mekk.fics.datatypes.game_type import GameType
from mekk.fics.datatypes.player import PlayerName
from mekk.fics.parsing.common import (
    rated_as_bool, numeric_rank, parse_detailed_date)

import re
from mekk.fics.datatypes.game_info import (
    GameInfo, GameSpec, ExaminedGame, ExaminedGameExt)
# import dateutil.parser

re_badgame = re.compile(r'''
    ^(
       The\scurrent\srange\sof\sgame\snumbers\sis\s\d+\sto\s\d+
       |
       There\sis\sno\sgame\s\d+\.
       |
       There\sis\sno\ssuch\sgame\.
     )''', re.VERBOSE)

re_ginfo_lead = re.compile(r'^Game (?P<game_no>\d+): Game information\.')
re_ginfo_players = re.compile(r"""
    ^\s*
    (?P<white>[^\s()]+)
    (?:\([A-Z]+\))*              # (C), (SR), ...
    \s*
    \((?P<white_rank>[-0-9]+)\)               # (2322), (----)
    \s+vs\s+
    (?P<black>[^\s()]+)
    (?:\([A-Z]+\))*              # (C), (SR), ...
    \s*
    \((?P<black_rank>[-0-9]+)\)               # (2322), (----)
    \s+
    ((?P<is_private>private)\s+)?
    (?P<is_rated>rated|unrated)
    \s+
    (?P<variant>\S+)
    \s+
    game\.
    """, re.VERBOSE)
#  anandkvs (2063) vs donnadistruttiva (1960) rated Standard game.
# starfishSuprise (1462) vs manok (1491) private rated Blitz game.

re_ginfo_examine = re.compile(r"""
    ^\s*
    (?P<examiner>[^\s()]+)
    (?:\([A-Z]+\))*              # (C), (SR), ...
    \s+
    is\s+examining
    \s+
    (?P<white>[^\s()]+)
    (?:\([A-Z]+\))*              # (C), (SR), ...
    \s+vs\s+
    (?P<black>[^\s()]+)
    (?:\([A-Z]+\))*              # (C), (SR), ...
    \s*
    \.
    """, re.VERBOSE)


re_ginfo_clock = re.compile(r"^\s*Time controls: (?P<base_sec>\d+) (?P<inc>\d+)")
re_ginfo_start_time = re.compile(r"^\s*Time of starting: (?P<start_time>.*)$")
#  White time 1:25:27    Black time 1:23:58
#  The clock is not paused
#  16 halfmoves have been made.
#  Fifty move count started at halfmove 13 (97 halfmoves until a draw).
#  White may castle both kingside and queenside.
#  Black may castle both kingside and queenside.
#  Double pawn push didn't occur.""")


re_bad_ginfo_player = re.compile(r'''
    ^
    (?P<player> [A-Za-z][A-Za-z0-9_]+)
    \s+
    (?P<message>
      is \s not \s logged .*
    |
      is \s not \s playing .*
    )
''', re.VERBOSE)


def parse_ginfo_reply(reply_text):
    """
    Parses output of "ginfo" command.

    :param reply_text: output text

    :return: game information structure
    :rtype: GameInfo
    """
    m = re_badgame.search(reply_text)
    if m:
        raise errors.NoSuchGame(reply_text)
    m = re_bad_ginfo_player.search(reply_text)
    if m:
        raise errors.BadPlayerState(m.group('player'),
                                    m.group('player') + ' '  + m.group('message'))
    lines = [l for l in reply_text.split("\n") if l]
    if len(lines) >= 4:
        m_lead = re_ginfo_lead.search(lines[0])
        m_players = re_ginfo_players.search(lines[1])
        if m_players:
            m_clock = re_ginfo_clock.search(lines[2])
            m_start_time = re_ginfo_start_time.search(lines[3])
            if m_lead and m_players and m_clock and m_start_time:
                start_time, start_time_zone = parse_detailed_date(m_start_time.group('start_time'))
                return GameInfo(
                    game_no=int(m_lead.group('game_no')),
                    white_name=PlayerName(m_players.group('white')),
                    black_name=PlayerName(m_players.group('black')),
                    white_rating_value=numeric_rank(m_players.group('white_rank')),
                    black_rating_value=numeric_rank(m_players.group('black_rank')),
                    start_time=start_time,
                    start_time_zone=start_time_zone,
                    game_spec=GameSpec(
                        game_type=GameType(m_players.group('variant')),
                        is_rated=rated_as_bool(m_players.group('is_rated')),
                        is_private=True if m_players.group('is_private') else False,
                        clock=GameClock(int(m_clock.group('base_sec'))/60,
                                        int(m_clock.group('inc')))))
        m_ex = re_ginfo_examine.search(lines[1])
        if m_ex:
            return ExaminedGameExt(
                game_no=int(m_lead.group('game_no')),
                examiner=PlayerName(m_ex.group('examiner')),
                white=PlayerName(m_ex.group('white')),
                black=PlayerName(m_ex.group('black')),
            )

    raise errors.ReplyParsingException(reply_text, "ginfo")

