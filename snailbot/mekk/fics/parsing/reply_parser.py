# -*- coding: utf-8 -*-

"""
Parsing replies to the commands.
"""

from mekk.fics.constants import block_codes
from mekk.fics import errors
from mekk.fics.datatypes.generic import GenericText, UnknownReply
from mekk.fics.parsing.reply.finger import parse_finger_reply
from mekk.fics.parsing.reply.observe import parse_observe_reply, parse_unobserve_reply
from mekk.fics.parsing.reply.games import parse_games_reply
from mekk.fics.parsing.reply.date import parse_date_reply
from mekk.fics.parsing.reply.list_operations import parse_showlist_reply, parse_handles_reply
from mekk.fics.parsing.reply.tell import parse_tell_reply
from mekk.fics.parsing.reply.seek import parse_seek_reply, parse_sought_reply
from mekk.fics.parsing.reply.lists import parse_addlist_reply, parse_sublist_reply
from mekk.fics.parsing.reply.play import parse_assess_reply, parse_made_move_reply
from mekk.fics.parsing.reply.info import parse_ginfo_reply
from mekk.fics.parsing.reply.who import parse_who_reply
from mekk.fics.parsing.reply.znotify import parse_znotify_reply

def forward_text(reply_text):
    """
    Used whenever we don't parse anything, just forward obtained text
    :param reply_text: FICS reply to sth.
    """
    return GenericText(text=reply_text.strip("\r\n "))

# maps command types to parsing routines
REPLY_DISPATCH={
    block_codes.BLKCMD_GAMES: ("games", parse_games_reply),
    block_codes.BLKCMD_OBSERVE: ("observe", parse_observe_reply),
    block_codes.BLKCMD_DATE: ("date", parse_date_reply),
    block_codes.BLKCMD_SET: ("set", forward_text), # TODO: maybe extract what was set
    block_codes.BLKCMD_UNOBSERVE: ("unobserve", parse_unobserve_reply),
    block_codes.BLKCMD_SHOWLIST: ("showlist", parse_showlist_reply),
    block_codes.BLKCMD_FINGER: ("finger", parse_finger_reply),
    block_codes.BLKCMD_QUIT: ("quit", forward_text),
    block_codes.BLKCMD_TELL: ("tell", parse_tell_reply),
    block_codes.BLKCMD_HANDLES: ("handles", parse_handles_reply),
    block_codes.BLKCMD_HELP: ("help", forward_text),
    block_codes.BLKCMD_NEXT: ("next", forward_text),
    block_codes.BLKCMD_SEEK: ("seek", parse_seek_reply),
    block_codes.BLKCMD_GAME_MOVE: ("moved", parse_made_move_reply),
    block_codes.BLKCMD_ASSESS: ("assess", parse_assess_reply),
    block_codes.BLKCMD_ADDLIST: ("addlist", parse_addlist_reply),
    block_codes.BLKCMD_SUBLIST: ("sublist", parse_sublist_reply),
    block_codes.BLKCMD_SOUGHT: ("sought", parse_sought_reply),
    block_codes.BLKCMD_GINFO: ("ginfo", parse_ginfo_reply),
    block_codes.BLKCMD_WHO: ("who", parse_who_reply),
    block_codes.BLKCMD_ZNOTIFY: ("znotify", parse_znotify_reply),
    #block_codes.BLKCMD_MESSAGES: ("messages", parse_messages_reply),
}

# maps error codes to appropriate exception classes
ERROR_DISPATCH = {
    block_codes.BLKCMD_ERROR_BADCOMMAND: errors.UnknownFicsCommand,
    block_codes.BLKCMD_ERROR_BADPARAMS: errors.BadFicsCommandParameters,
    block_codes.BLKCMD_ERROR_AMBIGUOUS: errors.AmbiguousFicsCommand,
    block_codes.BLKCMD_ERROR_RIGHTS: errors.InsufficientPermissions,
    block_codes.BLKCMD_ERROR_OBSOLETE: errors.UnknownFicsCommand,
    block_codes.BLKCMD_ERROR_REMOVED: errors.UnknownFicsCommand, # TODO: better exception?
    block_codes.BLKCMD_ERROR_NOTPLAYING: errors.AttemptToActOnNotPlayedGame,
    block_codes.BLKCMD_ERROR_NOSEQUENCE: errors.MissingBlockMarkers,
    block_codes.BLKCMD_ERROR_LENGTH: errors.BadFicsCommandSyntax,
}

def parse_fics_reply(command_type, reply_text):
    """
    General handling of FICS replies. Parses according to the
    command type and returns structured information.

    Returned data is a three-element tuple consisting of:

    - command name ("date", "observe", "games", ...)

    - boolean status: true if the command succeeded (and it's reply was
      succesfully parsed), false if the command failed (it's reply could
      not be parsed, or indicate failure)

    - if the command succeeded (status is True), object representing
      information associated with the reply, if the comand failed
      (status is False), exception object which can be thrown or logged

    Commands not (yet) handled are reported as unknowns (see last item below)
    and never fail

    Currently possible replies (command name and type of associated
    information):

    "observe", ObservedGame

    "unobserve", GameReference

    "games", GamesInProgress

    "set", GenericText
        Return to "set var sth" command. Field comment contains FICS reply string
        (for example "Style 12 set." or "You will no longer see seek ads.")

    "showlist", ListContents
        Return to commands like =td, =computer, =notify etc.

    "finger", FingerInfo
        Output to finger command.

    "handles", ListContents
        Return to "handles sb" command. Attribute .items is
        a list of PlayerName objects.

    "addlist", various (ChannelRef for +chan, PlayerName for +notify/+gnotify)

    "quit", GenericText
        Quit/logout.

    "unknown", UnknownReply
        None of the above (command not yet handled in this library or unhandled reply variant)

    :param command_type: code of the command as present in block mode (one
        of the BLKCMD_* constants)
    :param reply_text: actual complete reply text (possibly multiline)
    :return: tuple (command name, status, info dictionary) as described above
    """
    command_name, parsing_routine = REPLY_DISPATCH.get(command_type, (None,None))
    if parsing_routine:
        try:
            reply = parsing_routine(reply_text)
            if reply:
                return command_name, True, reply
            else:
                # Czegoś nie umiemy parsować
                return "unknown", True, UnknownReply(command_code=command_type,
                                                        reply_text=reply_text)
        except errors.FicsCommandException as exc:
            return command_name, False, exc
    else:
        if command_type < block_codes.LIMIT_BLKCMD_ERRORS:
            return "unknown", True, UnknownReply(command_code=command_type,
                                                 reply_text=reply_text)
        else:
            error_class = ERROR_DISPATCH.get(command_type, None)
            if error_class:
                return "unknown", False, error_class(reply_text)
            else:
                return "unknown", False, errors.FicsCommandException(reply_text)

# TODO: maybe some of those could be useful:
#
# Style 12 set.
# You are no longer receiving match requests.
# Highlight is off.
# You will not hear shouts.
# You will not hear cshouts.
# No such variable name seek.
# You will now hear kibitzes.
# You will now hear tells from unregistered users.
# You will now hear game results.
# You will now hear logins/logouts.
# Plan variable 1 changed to 'I am a bot'
# Width set to 1024.
# startpos set.)
# graph set.)
# You will not auto unobserve.)
# block set.
# You will not see seek ads
# :AGree(TM) t-shouts: Come on! 1 more player for 3 0 tourney and we start: "mam j 24"
# :mamer(TD) t-shouts: 1 0 r DRR tourney: "tell mamer JoinTourney 19" to join.

