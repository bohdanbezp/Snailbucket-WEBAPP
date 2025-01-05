# -*- coding: utf-8 -*-

"""
Parsing observe result.
"""

from mekk.fics.datatypes.game_type import GameType
from mekk.fics import errors
from mekk.fics.parsing.common import rated_as_bool, numeric_rank
from mekk.fics.datatypes.style12 import Style12
from mekk.fics.datatypes.game_clock import GameClock
from mekk.fics.datatypes.game_info import ObservedGame, GameSpec, GameReference
import re

# Sorry, game 125 is a private game

re_privategame = re.compile(r"Sorry, game (?P<game_no>\d+) is a private game")
re_maxgames = re.compile(r"You are already observing the maximum number of games")

# You are already observing game 77.
re_already_observing = re.compile(r"You are already observing game (?P<game_no>\d+)")

# Game 173: android (1789) CamyC (2021) rated blitz 3 0
# Game 25: risko (2624) cchess (2683) rated standard 15 0
# Game 176: Gidgiddoni (----) Juston (1120) rated standard 30 0
# (pojawia się przy gnotify a też w wyniku observe)
re_gamespec = re.compile(r"""
Game\s
(?P<game_no>\d+)
:\s
(?P<white>\w+)
\s \( \s*
(?P<white_rank>\d+|\-+|\++)
\) \s
(?P<black>\w+)
\s \( \s*
(?P<black_rank>\d+|\-+|\++)
\) \s
(?P<is_rated>rated|unrated)
\s
(?P<variant>[^\s]+)
\s
(?P<clock_base>\d+)
\s
(?P<clock_inc>\d+)
""", re.VERBOSE)

re_style12_emb = re.compile(r"^<12>\s(?P<style12>.*)$", re.MULTILINE)

# Removing game 138 from observation list.
re_unobserved = re.compile(r"Removing game (?P<game_no>\d+) from observation list")
# (used also in input processor, patch also there in case of changes)

# You are not observing any games.
# You are not observing game 13.
re_unobserved_fail = re.compile(
    r"""
    ^
    (?:
    You\sare\snot\sobserving\s(?:any\sgames|game\s\d+)
    |
    There\sis\sno\ssuch\sgame
    )
    """,
    re.VERBOSE)

re_no_such_game = re.compile(r"^There is no such game")

# Removing game 60 from observation list.
def parse_unobserve_reply(reply_text):
    """
    Parses reply to unobserve command. Reports failures as exceptions,
    otherwise returns game number
    :param reply_text: text to parse
    :return: GameReference object
    """
    m = re_unobserved.match(reply_text)
    if m:
        return GameReference(game_no=int(m.group('game_no')))
    elif re_unobserved_fail.match(reply_text):
        raise errors.AttemptToActOnNotUsedGame(reply_text, "unobserve")
    else:
        raise errors.ReplyParsingException(reply_text, "unobserve")


def parse_observe_reply(reply):
    """
    Parse reply to the "observe" command.

    If the command succeeded (we just started observing the game),
    returns ObservedGame object.

    If the command failed (game is private, game already finished etc)
    or we failed to parsing the text given, throws proper exception.
    """
    m = re_privategame.match(reply)
    if m:
        raise errors.AttemptToAccessPrivateGame("Can not observe private game " + m.group('game_no'))
    m = re_already_observing.match(reply)
    if m:
        raise errors.GameAlreadyObserved("Game already observed: " + m.group('game_no'))
    m = re_maxgames.match(reply)
    if m:
        raise errors.LimitExceeded("Can not observe more games, max achieved")
    m = re_no_such_game.search(reply)
    if m:
        raise errors.NoSuchGame("No such game")
    match_spec = re_gamespec.search(reply)
    if match_spec:
        match_pos = re_style12_emb.search(reply, match_spec.end(0) + 1)
        if match_pos:
            initial_style12 = Style12(match_pos.group('style12'))
            return ObservedGame(
                game_no=int(match_spec.group('game_no')),
                white_name=initial_style12.white,
                black_name=initial_style12.black,
                white_rating_value=numeric_rank(match_spec.group('white_rank')),
                black_rating_value=numeric_rank(match_spec.group('black_rank')),
                game_spec=GameSpec(
                    game_type=GameType(match_spec.group('variant')),
                    clock=GameClock(base_in_minutes=int(match_spec.group('clock_base')),
                                    inc_in_seconds=int(match_spec.group('clock_inc'))),
                    is_rated=rated_as_bool(match_spec.group('is_rated')),
                    is_private=False,
                    ),
                initial_style12=initial_style12,
            )
    raise errors.ReplyParsingException(reply, "observe")
