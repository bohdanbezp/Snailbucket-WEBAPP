# -*- coding: utf-8 -*-

"""
Game data
"""

from collections import namedtuple

#noinspection PyClassicStyleClass
class GameSpec(namedtuple("GameSpec",
    "game_type, clock, is_rated, is_private")):
    """
    Specification of the game, as used
    in games, observe result, or seeks. Contains:

    - game_type (GameType) - what game is it
    - clock (GameClock) - which clock is in use
    - is_rated (bool)
    - is_private (bool)
    """
    pass

#noinspection PyClassicStyleClass
class GameSpecPartial(namedtuple("GameSpecPartial", "game_type, is_rated")):
    """
    Limited spec of the game, as used in gin. Contains:

    - game_type (GameType) - what game is it
    - is_rated (bool)
    """
    pass

#noinspection PyClassicStyleClass
class ExaminedGame(namedtuple("ExaminedGame", "game_no")):
    """
    Information about running examine.

    game_no: int - FICS game number
    """
    pass

class ExaminedGameExt(namedtuple("ExaminedGame", "game_no, white, black, examiner")):
    """
    Information about running examine.

    game_no: int - FICS game number
    white: white name
    black: black name
    examiner: examining player name
    """
    pass

#noinspection PyClassicStyleClass
class SetupGame(namedtuple("SetupGame", "game_no")):
    """
    Information about running setup game.

    game_no: int - FICS game number
    """
    pass

#noinspection PyClassicStyleClass
class PlayedGame(namedtuple("PlayedGame",
    """
    game_no,
    white_truncated_name, black_truncated_name,
    white_rating_value, black_rating_value,
    game_spec
    """)):
    """
    Information about the game (usually the game in progress).
    Fields:

    - game_no (int) - FICS game id
    - white_truncated_name (PlayerName) - white's handle, can be truncated if it is long
    - black_truncated_name (PlayerName)
    - white_rating_value (int)
    - black_rating_value (int)
    - game_spec (GameSpec) - clock and variant info
    """
    pass

#noinspection PyClassicStyleClass
class GameInfo(namedtuple("GameInfo",
    """
    game_no,
    white_name, black_name,
    white_rating_value, black_rating_value,
    game_spec,
    start_time, start_time_zone
    """)):
    """
    Information about the game (usually the game in progress).
    Fields:

    - game_no (int) - FICS game id
    - white_name (PlayerName) - white's handle
    - black_name (PlayerName)
    - white_rating_value (int)
    - black_rating_value (int)
    - game_spec (GameSpec) - clock and variant info
    - start_time (datetime) - game start time acc. to FICS (zone-naive)
    - start_time_zone (str) - FICS name of timezone ("PDT", "EURCST" or so)
    """
    pass

#noinspection PyClassicStyleClass
class GameReference(namedtuple("GameReference", "game_no")):
    """
    Bare reference to some game, just it's id.

    game_no: int - FICS game number
    """
    pass

#noinspection PyClassicStyleClass
class ObservedGame(namedtuple("ObservedGame",
    """
    game_no,
    white_name, black_name,
    white_rating_value, black_rating_value,
    game_spec,
    initial_style12
    """)):
    """
    Information about the game being observed.
    Fields:

    - game_no (int) - FICS game id
    - white_name (PlayerName)
    - black_name (PlayerName)
    - white_rating_value (int)rated_as_bool
    - black_rating_value (int)
    - game_spec (GameSpec) - clock and variant info
    - initial_style12 (Style12) - board position
    """
    pass

#noinspection PyClassicStyleClass
class GamesInProgress(namedtuple("GamesInProgress",
    "games, examines, setups")):
    """
    games - list of PlayedGame objects (games being played)
    examines - list o ExaminedGame objects (examined games)
    setups - list of SetupGame objects (bsetup games)
    """
    pass
