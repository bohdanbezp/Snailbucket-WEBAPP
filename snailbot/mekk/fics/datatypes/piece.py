# -*- coding: utf-8 -*-

"""
Helper datatypes - piece related
"""

from mekk.fics import errors
from mekk.fics.datatypes.color import Color, WHITE, BLACK

class PieceName(object):
    """
    Representation of chess piece name (without color). Handles short and long
    names, resolves some ambiguities.
    """
    __slots__ = 'piece',

    _short2long = dict(K='king',
                       Q='queen',
                       R='rook',
                       B='bishop',
                       N='knight',
                       P='pawn')
    _long2short = dict(king='K',
                       queen='Q',
                       rook='R',
                       bishop='B',
                       knight='N',
                       pawn='P')

    def __init__(self, piece_text):
        """
        Constructor. Accepts both full-length strings ("queen", "rook", ...)
        and one-letter shortcuts ("Q", "B", "N", also lowercase "q", "b", "n").
        Can be also used as copy constructor.
        """
        if isinstance(piece_text, PieceName):
            self.piece = piece_text.piece
        else:
            if len(piece_text) == 1:
                pt = piece_text.upper()
                if pt in self._short2long:
                    self.piece = pt
                else:
                    raise errors.LibraryUsageException("Unknown piece: '%s'" % piece_text)
            else:
                pt = self._long2short.get(piece_text.lower())
                if pt:
                    self.piece = pt
                else:
                    raise errors.LibraryUsageException("Unknown piece: '%s'" % piece_text)

    @property
    def full_name(self):
        """
        Returns full name ("king", "queen", etc - all lowercase)
        """
        return self._short2long[self.piece]

    @property
    def short_name(self):
        """
        Returns shortcut ("K", "Q", etc - uppercase)
        """
        return self.piece

    def __str__(self):
        return self.full_name

    def __repr__(self):
        return "PieceName(%s)" % self.piece

    def __eq__(self, other):
        return self.piece == other.piece

    def __cmp__(self, other):
        return cmp(self.piece, other.piece)

    def __hash__(self):
        return hash(self.piece)

class Piece(object):
    """
    Representation of chess piece. Knows it's color (white
    or black) and actual name (king, queen, ...), handles
    conversions from/to FICS shorthand notation (lower/upper case
    single letter)
    """
    __slots__ = ('color', 'piece')

    def __init__(self, piece_name, color=None):
        """
        Constructor. Can be called in a few ways, depending on
        the parameters available:

        - if only piece_name is specified, it must be a FEN-like one letter
          piece designator - one of (k,q,r,b,n,p,K,Q,R,B,N,P), 
          uppercase letter means white, lowercase letter - black

        - if both piece_name and color are specified, piece_name can be
          a PieceName object, a one-letter string (in this case case does
          not matter) or full name (one of "king", "queen", … - case does
          not matter), and color designates the piece color.

        :param piece_name: single-letter notation (k,q,r,b,n,p,K,Q,R,B,N,P,
            if color is missing uppercase means white while lowercase - black),
            full name notation ("king", "queen", "rook", "bishop", "knight", "pawn"),
            or PieceName object
        :type piece_name: str or PieceName
        :param color: single-letter notation (w or b), full name (white or black),
            or Color object. In case of strings case does not mater.
        :type color: Color or str
        """
        self.piece = PieceName(piece_name)
        if color:
            self.color = Color(color)
        else:
            #noinspection PyTypeChecker
            if isinstance(piece_name, str) and (len(piece_name) == 1):
                if piece_name.isupper():
                    self.color = WHITE
                else:
                    self.color = BLACK
            else:
                raise errors.LibraryUsageException("Piece requires one-letter piece shortcut when color is not given, but something else specified: '%s'" % repr(piece_name))

    def shortcut(self):
        """
        Returns FEN-like notation (N for white knight, b for black bishop etc)
        """
        if self.color.is_white():
            return self.piece.short_name.upper()
        else:
            return self.piece.short_name.lower()

    def __str__(self):
        return "%s %s" % (self.color, self.piece)

    def __repr__(self):
        return "Piece(%s,%s)" % (repr(self.color), repr(self.piece))

    def __eq__(self, other):
        return (self.color == other.color) and (self.piece == other.piece)

    def __cmp__(self, other):
        return cmp(self.color, other.color) or cmp(self.piece, other.piece)

    def __hash__(self):
        return hash((self.color, self.piece))

