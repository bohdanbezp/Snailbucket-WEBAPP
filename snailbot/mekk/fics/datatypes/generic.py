# -*- coding: utf-8 -*-

"""
Helper datatypes - raw text.
"""

from collections import namedtuple

#noinspection PyClassicStyleClass
class GenericText(namedtuple("GenericText", "text")):
    """
    Generic FICS text representation. The only attribute,

    text

    contains complete FICS text (notification line, command
    reply etc).

    This type is mostly used to mark that given kind of command
    is not parsed in a better way.
    """
    def __str__(self):
        # On purpose we do not escape \n etc, looks better
        # in interactive console and such.
        return "GenericText(text='%s')" % self.text

#noinspection PyClassicStyleClass
class UnknownReply(namedtuple("UnknownReply", "command_code, reply_text")):
    """
    Information about unknown (not-yet-parsed by mekk.fics) reply to some command.

    :command_code:
        Numerical command code (one of BLKCMD_* constants)
    :reply_text:
        Complete reply text
    """
    pass
