# -*- coding: utf-8 -*-

"""
Piece color in many ways
"""

from mekk.fics import errors

class Color(object):
    """
    Representation of the color (white or black). Handles
    conversions from short (w/b) to long form and negation.
    """
    __slots__ = 'color',

    def __init__(self, color):
        """
        Constructor. Accepts both full-length strings ("white", "black")
        and one-letter shortcuts ("w", "b", also uppercase "W", "B").
        Also works as copy constructor.
        """
        if isinstance(color, Color):
            self.color = color.color
        else:
            color = color.lower()
            if color in ['w', 'white']:
                self.color = 'white'
            elif color in ['b', 'black']:
                self.color = 'black'
            else:
                raise errors.LibraryUsageException("Unknown color: '%s'" % color)

    def negate(self):
        """
        Returns color opposite to this one
        """
        if self.color == 'white':
            return Color('black')
        else:
            return Color('white')

    @property
    def is_white(self):
        return self.color == 'white'

    @property
    def is_black(self):
        return self.color == 'black'

    def __str__(self):
        return self.color

    def __repr__(self):
        return "Color(%s)" % self.color

    def __eq__(self, other):
        return self.color == other.color

    def __cmp__(self, other):
        return cmp(self.color, other.color)

    def __hash__(self):
        return hash(self.color)

WHITE = Color('white')
BLACK = Color('black')
