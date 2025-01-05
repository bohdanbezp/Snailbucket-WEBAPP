# -*- coding: utf-8 -*-

"""
Objects used to represent different information received from FICS.
"""

from collections import namedtuple
import re

#noinspection PyClassicStyleClass
class AttributedTell(namedtuple("AttributedTell", "player, text")):
    """
    Info about direct tell, shout, cshout, announcement - in general,
    attributed text said.

    - player (PlayerName) - tell author
    - text (string) - text told
    """
    pass

#noinspection PyClassicStyleClass
class ChannelTell(namedtuple("ChannelTell", "player, channel, text")):
    """
    Info about channel tell

    - player (PlayerName) - tell author,
    - channel (int) - channel number,
    - text (string) - text told
    """
    pass

#noinspection PyClassicStyleClass
class GameMove(namedtuple("GameMove", "style12")):
    """
    Info about game move

    - style12 (Style12) - move representation
    """
    pass

#noinspection PyClassicStyleClass
class CompressedMove(namedtuple("CompressedMove", "game_no, half_moves_count, algebraic, smith, time_taken, time_left")):
    """
    Info about game move - when compressed moves are in use. 

    Note: time_taken and time_left are datetime.timedelta objects.
    """
    pass

#noinspection PyClassicStyleClass
class GameKibitz(namedtuple("GameKibitz",
                            "game_no, player, rating_value, method, text")):
    """
    Info about game kibitz/whisper

    - game_no (int) - game number,
    - player (PlayerName) - tell author,
    - rating_value (int) - player's rating
    - method (string) - 'whispers' or 'kibitzes'
    - text (string) - text told
    """
    def is_whisper(self):
        """
        Is it a whisper? (comment invisible to players)
        """
        return self.method == 'whispers'
    def is_kibitz(self):
        """
        Is it kibitz? (comment visible to everybody)
        """
        return self.method == 'kibitzes'

# TODO: classify some of those notes
#noinspection PyClassicStyleClass
class GameNote(namedtuple("GameNote", "game_no, note")):
    """
    Info about game non-move event

    - game_no (int) - game number,
    - note (string) - information (used for things
      like draw/abort/pause/adjourn/... offers, replies to those offers,
      notifications about moretime, takeback etc)
    """
    pass

#class GameStarted(namedtuple("GameStarted", "")):
#    """
#    game_no:i - game number, white:s - player name, black:s - player name,
#    is_rated:b - is game rated, variant:s - game variant (”standard”, ”blitz”, ”wild/fr” etc)
#    """
#    pass


# Useless aborted games: abort on first move etc.
#
# aesha lost connection and too few moves; game aborted
# Game aborted on move 1
# Hono lost connection and too few moves; game aborted
re_early_abort = re.compile(r"""
(?:
\w+lost\sconnection\sand\stoo\sfew\smoves;\sgame\saborted
|
Game\saborted\son\smove\s1
|
\w+\slost\sconnection\sand\stoo\sfew\smoves;\sgame\saborted
)
""", re.VERBOSE)

#noinspection PyClassicStyleClass
class GameFinish(namedtuple(
        "GameFinish",
        "game_no, white_name, black_name,result,result_desc")):
    """
    Info about game finish and result:

    - game_no (int) - game number,
    - white_name (PlayerName)
    - black_name (PlayerName)
    - result (string) - one of "1-0","0-1","1/2-1/2","*"
    - result_desc (string) - textual description of the result (like ”White resigned")
    - early_abort (bool) - true when the game is aborted on move 1
    """
    pass

    @property
    def early_abort(self):
        """
        Checks (by looking at result_desc) whether this game looks like aborted on move 1
        or similarly aborted early.
        """
        if re_early_abort.match(self.result_desc):
            return True
        else:
            return False

#class ObservingFinished(namedtuple("ObservingFinished", "")):
#    """
#    game_no:i - game number
#    """
#    pass


#noinspection PyClassicStyleClass
class GameStart(namedtuple("GameStart",
    """
    game_no,
    white_name, black_name,
    game_spec
    """)):
    """
    Information about the game being observed.
    Fields:

    - game_no (int) - FICS game id
    - white_name (PlayerName)
    - black_name (PlayerName)
    - game_spec (GameSpecPartial) - variant and ratedness info
    - initial_style12 (Style12) - board position
    """
    pass


class GameStartExt(namedtuple("GameStartExt",
    """
    game_no, white_name, white_rating, black_name, black_rating
    game_spec
    """)):
    """
    Information about the game started (rich info provided
    via some lists).
    Fields:

    - game_no (int) - FICS game id
    - white_name (PlayerName)
    - black_name (PlayerName)
    - white_rating
    - black_rating
    - game_spec (GameSpec) - variant, clock and ratedness info
    """
    pass


#noinspection PyClassicStyleClass
class GameJoinInfo(namedtuple("GameJoinInfo",
    "game_no, game_spec, white_rating, black_rating")):
    """
    Alternative info about started game, used in iv_gameinfo mode.
    Fields:

    - game_no (int) - FICS game id
    - game_spec (GameSpec) - variant, clock and ratedness info
    - white_rating
    - black_rating
    """
    pass

#noinspection PyClassicStyleClass
class Seek(namedtuple("Seek", "seek_no, player, player_rating_value, is_manual, using_formula, color, game_spec")):
    """
    Information about issued seek:

    seek_no:
        (`int`) FICS seek id
    player:
        (`PlayerName`) Who issued the seek?
    player_rating_value:
        (`int`) Current rating of this player.
    is_manual:
        (`bool`) Is seek flagged as manual?
    using_formula:
        (`bool`) Is seeker using formula to filter offers?
    color:
        (`Color`) Color expected by the person who issued the seek (WHITE or BLACK). None if no such
        request is in place.
    game_spec:
        (`GameSpec`) Game features (variant, clock, ratedness)
    """
    # TODO: rating range
    pass

#noinspection PyClassicStyleClass
class SeekRef(namedtuple("SeekRef", "seek_no")):
    """
    Reference to some (active) seek. Just:

    - seek_no (int) - seek id
    """
    pass
