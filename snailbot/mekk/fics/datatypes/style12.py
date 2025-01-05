# -*- coding: utf-8 -*-

"""
Parse and represent style12 move/game information.
"""

import re
from mekk.fics.datatypes.color import Color
from mekk.fics.datatypes.game_clock import GameClock
from mekk.fics.datatypes.player import PlayerName
from mekk.fics.errors import BadStyle12Format

OBSERVER_WATCHING_ISOLATED_POSITION = -3
OBSERVER_WATCHING_EXAMINE = -2
OBSERVER_EXAMINING = 2
OBSERVER_PLAYING_WITH_OPP_TO_MOVE = -1
OBSERVER_PLAYING_AND_TO_MOVE = 1
OBSERVER_WATCHING_GAME = 0

#TODO: review fields, consider extracting some datatypes
#      instead of so many methods

class Style12(object):
    """
    Representation of Style12 line (FICS-specifics move and game
    status information). Provides raw data values, some extra
    properties, and some higher level helper methods.

    The following attributes and properties are provided.

    General game metadata
    ---------------------------------

    game_no:
       (`int`) FICS game number.
    white:
       (`PlayerName`) white player
    black:
       (`PlayerName`) black player
    clock:
       (`GameClock`) clock info

    Current game status
    ---------------------------------

    side_to_move:
       (`str`) who is to move ('B' - black, 'W' - white)
    to_move:
       (`Color`) also who is to move, but as `Color` object (easy to negate and
       comparable with constants WHITE and BLACK from mekk.fics.datatypes.color)
    after_double_push:
       (`str`) if we are just after double pawn move, name of column ("a"-"h"), otherwise None
    can_white_castle_short:
       (`bool`) is castling k-side by white still possible?
    can_white_castle_long:
       (`bool`) is castling q-side by white still possible?
    can_black_castle_short:
       (`bool`) is castling k-side by black still possible?
    can_black_castle_long:
       (`bool`) is castling q-side by black still possible?
    reversible_plies_count:
       (`int`) plies (half-moves) since last move of some pawn or any capture 
       (to be used in 50-moves rule checking)
    is_clock_running:
       (`bool`) as name suggests... (false if the game is paused)
    white_material:
       (`int`) value of white's  material (initial setup is worth 39)
    black_material:
       (`int`) value of black's  material (initial setup is worth 39)
    white_remaining_time:
       (`int`) remaining white's time in seconds
    black_remaining_time:
       (`int`) remaining black's time in seconds

    next_move_no:
       (`int`) next move number (as in chess books, there are two first moves, then two second etc)
       moves etc)
    last_move_no:
       (`int`) last move number, i.e. number of move just executed, represented by this object
       (also as in chess books, the same number appears for white, then for black)
    last_ply_no:
       (`int`) last half-move number, i.e. number of half-move just executed, represented by this object
       (after 1.e4 e5 2.Nf3 we have last_ply_no=3)

    is_move:
       (`bool`) is this object representing true move (false on beginning of the game and some similar cases)
    last_move_text:
       (`str`) last move in normal form ("c4", "Ke2", "O-O", ...
    last_move_coord_text:
       (`str`) last move in elaborate form ("P/c2-c4", "K/e1-e2")
       Note: castling uses small-letters (o-o or o-o-o)
    last_move_time_spent:
       (`int`) Time spent thinking on last move, in seconds
    last_move_time_spent_text:
       (`str) Time spent thinking on last move, as (min:sec) text, for example "(2:17)"

    last_move_lag:
       (`int`) Last move lag in miliseconds

    Position info
    ---------------------------------

    str:
       (`str`) Convert back to style12 line ("<12> ....")

    fen:
       (`str`) Return position in FEN notation.

    is_standard_initial_position:
       (`bool`) Is this initial chess setup?

    Game watching context
    ---------------------------------

    observer_role:
        (`int`) Our (observer) relation to the game, one of the following constants:

        - OBSERVER_WATCHING_ISOLATED_POSITION - "spos" command effect or similar case, isolated position

        - OBSERVER_WATCHING_EXAMINE - we watch game examined by somebody else

        - OBSERVER_EXAMINING - we examine the game

        - OBSERVER_PLAYING_WITH_OPP_TO_MOVE - we play the game, opponent is to move,

        - OBSERVER_PLAYING_AND_TO_MOVE - we play the game and we are to move

        - OBSERVER_WATCHING_GAME - we observe the game played by others
        
    board_flipped:
         (`bool`) is board flipped?

    """
    __slots__ = (
        # Board rows, in s12 format
        '_row1', '_row2', '_row3', '_row4', '_row5', '_row6', '_row7', '_row8',
        # Who is to move - B or W
        'side_to_move',
        # Are we after double pawn move - -1 if not, 0-7 if yes (column number)
        '_double_push',
        # Is castling allowed
        'can_white_castle_short', 'can_white_castle_long','can_black_castle_short','can_black_castle_long',
        # Half-moves since last irreversible move (100 such allow draw claim)
        'reversible_plies_count',
        # Game number
        'game_no',
        # Player names
        'white', 'black',
        # Observer relation to the game (-3 - isolated position/spos, -2 observing examine, 2 examining, -1 playing and opp is to move, 1 playing and he is to move, 0 observing game in play)
        'observer_role',
        # Base clock and increment (both in seconds)
        'clock',
        # Material numeric value
        'white_material', 'black_material',
        # Remaining time (in seconds)
        'white_remaining_time', 'black_remaining_time',
        # Next move number (as in chess, there are two 1-st moves etc)
        'next_move_no',
        # Last move notation (verbose coordinate)
        'last_move_coord_text',
        # Time spent on last move (min:sec)
        'last_move_time_spent_text',
        'last_move_time_spent',
        # Last move notation (pretty, None if there was no move)
        'last_move_text',
        # Is board flipped?
        'board_flipped',
        # Is clock running?
        'is_clock_ticking',
        # Lag in miliseconds (for last move)
        'last_move_lag')
    def __init__(self, style12_text):
        """
        Parses and stores s12 line.

        :Parameters:
        style12_text - parsed line. Both format with leading <12>
            and without it is supported ("<12> rnb…" and "rnb…" are both OK)
        """
        fields = style12_text.split(' ')
        if fields[0] == "<12>":
            fields = fields[1:]
        if len(fields) < 32:
            raise BadStyle12Format("Invalid S12 line: %s" % style12_text)
        self._row1 = fields[0]
        self._row2 = fields[1]
        self._row3 = fields[2]
        self._row4 = fields[3]
        self._row5 = fields[4]
        self._row6 = fields[5]
        self._row7 = fields[6]
        self._row8 = fields[7]
        self.side_to_move = fields[8]
        self._double_push = int(fields[9])
        self.can_white_castle_short = bool(int(fields[10]))
        self.can_white_castle_long = bool(int(fields[11]))
        self.can_black_castle_short = bool(int(fields[12]))
        self.can_black_castle_long = bool(int(fields[13]))
        self.reversible_plies_count = int(fields[14])
        self.game_no = int(fields[15])
        self.white = PlayerName(fields[16])
        self.black = PlayerName(fields[17])
        self.observer_role = int(fields[18])
        self.clock = GameClock(
            base_in_minutes=int(fields[19]),
            inc_in_seconds=int(fields[20]))
        self.white_material = int(fields[21])
        self.black_material = int(fields[22])
        self.white_remaining_time = int(fields[23])
        self.black_remaining_time = int(fields[24])
        self.next_move_no = int(fields[25])
        self.last_move_coord_text = _detect_none(fields[26])
        self.last_move_time_spent_text = fields[27]
        self.last_move_time_spent = _unpack_time_spent(self.last_move_time_spent_text)
        self.last_move_text = _detect_none(fields[28])
        self.board_flipped = bool(int(fields[29]))
        self.is_clock_ticking = bool(int(fields[30]))
        self.last_move_lag = int(fields[31])

    def __eq__(self, other):
        # TODO: rewrite in non-lazy way, fieldwise
        return str(self) == str(other)

    @property
    def to_move(self):
        return Color(self.side_to_move)
#    @property
#    def is_white_to_move(self):
#        """Will *next* move be white's?"""
#        return self.side_to_move == "W"
#    @property
#    def is_black_to_move(self):
#        """Will *next* move be black?"""
#        return self.side_to_move == "B"
#    @property
#    def is_last_white_move(self):
#        """Is *this* move white's?"""
#        return self.side_to_move == "B"
#    @property
#    def is_last_black_move(self):
#        """Is *this* move black's?"""
#        return self.side_to_move == "W"

    @property
    def is_after_double_push(self):
        if self._double_push != -1:
            return self._double_push + 1
        else:
            return None

    @property
    def after_double_push(self):
        if self._double_push < 0:
            return None
        else:
            return chr(ord("a") + self._double_push)

    def __repr__(self):
        return "Style12(%s)" % str(self)

    def __str__(self):
        data = [
            self._row1,self._row2,self._row3,self._row4,self._row5,self._row6,self._row7,self._row8,
            self.side_to_move,
            str(self._double_push),
            self.can_white_castle_short and '1' or '0',
            self.can_white_castle_long and '1' or '0',
            self.can_black_castle_short and '1' or '0',
            self.can_black_castle_long and '1' or '0',
            str(self.reversible_plies_count),
            str(self.game_no),
            self.white.name, self.black.name,
            str(self.observer_role),
            str(self.clock.base_min), str(self.clock.inc_sec),
            str(self.white_material), str(self.black_material),
            str(self.white_remaining_time), str(self.black_remaining_time),
            str(self.next_move_no),
            self.last_move_coord_text or 'none',
            self.last_move_time_spent_text,
            self.last_move_text or 'none',
            self.board_flipped and '1' or '0',
            self.is_clock_ticking and '1' or '0',
            str(self.last_move_lag),
            ]
        #print data
        return "<12> " +  " ".join(data)

    @property
    def last_move_no(self):
        if self.side_to_move == "B":
            return self.next_move_no
        else:
            return self.next_move_no - 1

    @property
    def last_ply_no(self):
        if self.side_to_move == "B":
            return self.next_move_no * 2 - 1
        else:
            return self.next_move_no * 2 - 2

    @property
    def is_move(self):
        return self.last_move_text is not None

    @property
    def is_standard_initial_position(self):
        """Zwraca info czy mamy tu standardową pozycję przed pierwszym ruchem. Używane np. do stwierdzania, czy trzeba
           FEN w PGN umieścić."""
        return self._row1 == 'rnbqkbnr' and self._row2 == 'pppppppp' \
               and self._row3 == '--------' and self._row4 == '--------' \
               and self._row5 == '--------' and self._row6 == '--------' \
               and self._row7 == 'PPPPPPPP' and self._row8 == 'RNBQKBNR' \
               and self.side_to_move == 'W' \
               and self.can_white_castle_short and self.can_white_castle_long \
               and self.can_black_castle_short and self.can_black_castle_long \
               and self._double_push == -1

    @property
    def fen(self):
        """Zwraca pozycję przed ruchem w zapisie FEN"""
        # Zapis w style12 i fen się niemal zgadza - trzeba jedynie zastąpić minusy/ciągi minusów
        # cyframi opisującymi ich ilość
        fen_pos = '/'.join([_s12row_to_fenrow(x) for x in (self._row1, self._row2, self._row3, self._row4,
                                                           self._row5, self._row6, self._row7, self._row8)])
        to_move = self.side_to_move.lower()
        castling = ''
        if self.can_white_castle_short:
            castling += 'K'
        if self.can_white_castle_long:
            castling += 'Q'
        if self.can_black_castle_short:
            castling += 'k'
        if self.can_black_castle_long:
            castling += 'q'
        if not castling:
            castling = '-'
        enpassant = '-'
        if self._double_push != -1:
            enpfile = chr(ord('a')+self._double_push)
            enpline = '3'
            if self.side_to_move == 'W':
                enpline = '6'
            enpassant = enpfile + enpline
        halfmove_clock = str(self.reversible_plies_count)
        next_move_no = str(self.next_move_no)
        return ' '.join([ fen_pos, to_move, castling, enpassant, halfmove_clock, next_move_no])

def is_the_same_move(s1, s2):
    """
    Compares two moves represented as Style12 objects. Returns true
    if those objects correspond to the same move of the same game.

    Only game related information is used. Things like rating
    change are ignored.

    Intended use: detection of move repeats (like after refresh
    or when two clients watch the same game)
    """
    assert(isinstance(s1, Style12))
    assert(isinstance(s2, Style12))
    return s1._row1 == s2._row1 \
        and s1._row2 == s2._row2 \
        and s1._row3 == s2._row3 \
        and s1._row4 == s2._row4 \
        and s1._row5 == s2._row5 \
        and s1._row6 == s2._row6 \
        and s1._row7 == s2._row7 \
        and s1._row8 == s2._row8 \
        and s1.last_move_coord_text == s2.last_move_coord_text \
        and s1.last_move_time_spent_text == s2.last_move_time_spent_text \
        and s1.white == s2.white \
        and s1.black == s2.black \
        and s1.game_no == s2.game_no \
        and s1.clock == s2.clock \
        and s1.next_move_no == s2.next_move_no

re_minuses = re.compile(r'\-+')
def _s12row_to_fenrow(l):
    """Convert of single row from S12 to FEN"""
    # Just replace chains of minuses with number - their count
    return re_minuses.sub(lambda x: str(len(x.group())), l)

re_timespent = re.compile(r'^\((\d+):(\d+)\)')
re_long_timespent = re.compile(r'^\((\d+):(\d+):(\d+)\)')    # (1:02:28)
def _unpack_time_spent(t):
    m = re_timespent.match(t)
    if m:
        return int(m.group(1)) * 60 + int(m.group(2))
    m = re_long_timespent.match(t)
    if m:
        return int(m.group(1)) * 60 * 60 + int(m.group(2)) * 60 + int(m.group(3))

    raise BadStyle12Format("Text '%s' does not look like time spent spec, expected sth like '(1:17)'" % t)
    
def _detect_none(txt, none_value = "none"):
    if txt == none_value:
        return None
    else:
        return txt
