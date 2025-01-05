# -*- coding: utf-8 -*-

"""
Generating ivar-login line (login which sets some variables)
"""

from mekk.fics.errors import UnknownIVar

######################################################################
# ivars
######################################################################

# ivar-login
# On the login line, you're supposed to send "%b" followed by a string
# of ones and zeroes specifying the ivars setting, followed by a newline.
# Each bit specifies a single ivar in the following order:
#   PFLAG_I_COMPRESSMOVE, PFLAG_I_AUDIOCHAT, PFLAG_I_SEEKREMOVE, PFLAG_I_DEFPROMPT,
#   PFLAG_I_LOCK, PFLAG_I_STARTPOS, PFLAG_I_BLOCK, PFLAG_I_GAMEINFO, PFLAG_I_XDR,
#   PFLAG_I_PENDINFO, PFLAG_I_GRAPH, PFLAG_I_SEEKINFO, PFLAG_I_EXTASCII,
#   PFLAG_I_NOHIGHLIGHT, PFLAG_I_VT_HIGHLIGHT, PFLAG_I_SHOWSERVER, PFLAG_I_PIN,
#   PFLAG_I_MS, PFLAG_I_PINGINFO, PFLAG_I_BOARDINFO, PFLAG_I_EXTUSERINFO,
#   PFLAG_I_SEEKCA, PFLAG_I_SHOWOWNSEEK, PFLAG_I_PREMOVE, PFLAG_I_SMARTMOVE,
#   PFLAG_I_MOVECASE, PFLAG_I_SUICIDE, PFLAG_I_CRAZYHOUSE, PFLAG_I_LOSERS,
#   PFLAG_I_WILDCASTLE, PFLAG_I_FR, PFLAG_I_NOWRAP, PFLAG_I_ALLRESULTS, PFLAG_I_OBSPING,
#   PFLAG_I_SINGLEBOARD,
# For example, "%b1001" will set COMPRESSMOVE and DEFPROMPT on.
# You then send the username/password as usual.

IVAR_FLAGS = ('COMPRESSMOVE', 'AUDIOCHAT', 'SEEKREMOVE', 'DEFPROMPT',
              'LOCK', 'STARTPOS', 'BLOCK', 'GAMEINFO', 'XDR',
              'PENDINFO', 'GRAPH', 'SEEKINFO', 'EXTASCII',
              'NOHIGHLIGHT', 'VT_HIGHLIGHT', 'SHOWSERVER', 'PIN',
              'MS', 'PINGINFO', 'BOARDINFO', 'EXTUSERINFO',
              'SEEKCA', 'SHOWOWNSEEK', 'PREMOVE', 'SMARTMOVE',
              'MOVECASE', 'SUICIDE', 'CRAZYHOUSE', 'LOSERS',
              'WILDCASTLE', 'FR', 'NOWRAP', 'ALLRESULTS', 'OBSPING',
              'SINGLEBOARD')


def ivar_login_line(*args):
    """
    Constructs ivar's line consisting of given flags (without trailing \n).
    Such line must be sent (together with \n) before username during login.

    Routine parameters: list of flags to set (any of strings from IVAR_FLAGS).

    >>> ivar_login_line('DEFPROMPT', 'BLOCK')
    '%b0001001'

    """
    bits = [ivar in args and '1' or '0' for ivar in IVAR_FLAGS]
    # Weryfikacja czy nie podano czegoś niepoprawnego
    for arg in args:
        if not arg in IVAR_FLAGS:
            raise UnknownIVar(arg)
    return '%b' + "".join(bits).rstrip('0')
