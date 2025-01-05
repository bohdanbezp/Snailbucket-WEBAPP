# -*- coding: utf-8 -*-

"""
mekk.fics.datatypes
====================

mekk.fics.datatypes defines many small and
a few slightly bigger datatypes used in replies
from FICS commands.

Those datatypes are to a degree overkill,
returning tuple or dictionary from appropriate
functions would work as well, but they make
it easier to document appropriate structures,
avoid typing errors, and inspect data.
"""

# TODO: import all datatypes here to save some typing

#from mekk.fics.datatypes.date import FicsDateInfo

from mekk.fics.datatypes.channel import ChannelRef
from mekk.fics.datatypes.color import Color, BLACK, WHITE
from mekk.fics.datatypes.date import FicsDateInfo
from mekk.fics.datatypes.game_clock import GameClock
from mekk.fics.datatypes.game_info import GameInfo, GameReference, GamesInProgress, GameSpec,\
    GameSpecPartial, SetupGame, PlayedGame, ExaminedGame, ExaminedGameExt, ObservedGame
from mekk.fics.datatypes.game_type import GameType
from mekk.fics.datatypes.generic import GenericText, UnknownReply
from mekk.fics.datatypes.list_items import ListContents
from mekk.fics.datatypes.notifications import AttributedTell, ChannelTell, GameMove, \
    CompressedMove, GameKibitz, GameNote, GameFinish, GameStart, GameJoinInfo, \
    Seek, SeekRef, GameStartExt
from mekk.fics.datatypes.piece import PieceName, Piece
from mekk.fics.datatypes.player import PlayerName, PlayerRating, ResultStats, FingerInfo, ZnotifyInfo, IdleInfo
from mekk.fics.datatypes.style12 import Style12, BadStyle12Format
