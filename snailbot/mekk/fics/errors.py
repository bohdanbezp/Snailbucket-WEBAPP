# -*- coding: utf-8 -*-

"""
mekk.fics exception classes

Exceptions constitute small hierarchy.  This module applies the
following convention:

    - exceptions which have names ending with Exception are not
      directly thrown (those are grouping base classes which allow
      catching/handling groups of errors)

    - remaining exceptions represent specific errors.
"""

# pylint: disable=line-too-long

############################################################################

class FicsClientException(Exception):
    """
    General base class for all exceptions thrown by mekk.fics
    """
    pass

class InteractionException(FicsClientException):
    """
    Errors related to interactive input
    """
    pass

############################################################################

class LibraryUsageException(FicsClientException):
    """
    All cases of bad/unknown parameters, calls in inappropriate context etc.
    """
    pass


class FingerTooLong(LibraryUsageException):
    """Attempt to create finger notes of more than 10 lines"""
    pass


class UnknownIVar(LibraryUsageException):
    """Attempt to set unknown ivar-iable"""
    pass


############################################################################

class FicsCommandException(FicsClientException):
    """
    Exception thrown in case we can not confirm succesful FICS
    command execution and provide the results - either because
    command failed, or because we are unable to parsing the results.
    """
    def __init__(self, message):
        FicsClientException.__init__(self, message)


class ReplyParsingException(FicsCommandException):
    """Exception thrown in case we can't parsing output of some command"""
    def __init__(self, troublesome_text, command=None):
        if len(troublesome_text) > 240:
            troublesome_text = troublesome_text[:100] + "(...parts omitted...)" + troublesome_text[-100:]
        if not command:
            command = "???"
        FicsCommandException.__init__(
            self, "Couldn't parse command {0:>s} output. Problematic text:\n{1:>s}".format(
                command, troublesome_text))


# Note: this error is not strictly and always FicsCommand, may arise
# also on notifications, but most often it happens in observe reply so let's
# keep it here to behave properly in reply parsing.

class BadStyle12Format(FicsCommandException):
    """Failed to parse style12 string"""
    pass


class FicsCommandExecutionException(FicsCommandException):
    """Exception thrown in case command output indicates failure
    (for example when we try to observe private game or attempt
    action we don't have right to execute)"""
    def __init__(self, failure_reason, command=None):
        if command:
            command_info = "Command " + command
        else:
            command_info = "Command"
        FicsCommandException.__init__(
            self, command_info + " failed: " + failure_reason.strip(" \r\n"))


class FicsCommandTimedOut(FicsCommandException):
    """Some command did not provide reply in expected time"""
    def __init__(self, elapsed_time):
        FicsCommandException.__init__(
            self, "No reply obtained within %s" % str(elapsed_time))


class AttemptToAccessPrivateGame(FicsCommandExecutionException):
    """Attempt to observe or otherwise access private game"""
    pass


class GameAlreadyObserved(FicsCommandExecutionException):
    """Attempt to observe already observed game"""
    pass


class LimitExceeded(FicsCommandExecutionException):
    """Too many observed games, too many items on some list etc etc"""
    pass


class AttemptToActOnNotUsedGame(FicsCommandExecutionException):
    """Attempt to perform action which require participation (playing or observing)
    on the game on which we are not participating"""
    pass


class AttemptToActOnNotPlayedGame(FicsCommandExecutionException):
    """Attempt to perform action which is restricted to game players/examiners (like making a move)
    on the game we do not play (or while playing)"""
    pass


class NoSuchGame(FicsCommandExecutionException):
    """Attempt to perform some action on the game which does not exist"""
    pass


class UnknownList(FicsCommandExecutionException):
    """Attempt to use (=list, +list sth, ...) the list which does not exist"""
    def __init__(self, list_name):
        FicsCommandExecutionException.__init__(self, "Unknown list: %s" % list_name)
        self.list_name = list_name


class AlreadyOnList(FicsCommandExecutionException):
    """Cases like adding player to the list he or she is already on, subscribing
    already subscribed channel etc."""
    def __init__(self, item, list_name):
        FicsCommandExecutionException.__init__(
            self, "%s is already on your %s list" % (item, list_name))
        self.list_name = list_name
        self.item = item


class NotOnList(FicsCommandExecutionException):
    """Cases like removing player from list he is not on, unsubscribing
    not subscribe channel ect."""
    def __init__(self, item, list_name):
        FicsCommandExecutionException.__init__(
            self, "%s is not in your %s list" % (item, list_name))
        self.list_name = list_name
        self.item = item


class UnknownPlayer(FicsCommandExecutionException):
    """Attempt to use (finger, history, ...) non-existing player"""
    def __init__(self, player_name):
        FicsCommandExecutionException.__init__(self, "Unknown player: %s" % player_name)
        self.player_name = player_name


class AmbiguousPlayer(FicsCommandExecutionException):
    """Attempt to use (finger, history, ...) non-unique name"""
    def __init__(self, player_name, matching_players=None):
        FicsCommandExecutionException.__init__(self, "Ambiguous player: %s" % player_name)
        self.player_name = player_name
        if matching_players:
            self.matching_players = matching_players  # Can be missing
        else:
            self.matching_players = []


class BadPlayerState(FicsCommandExecutionException):
    """
    Attempt to perform player-related action on some player who
    is not in appropriate state (like attempt to watch a game of the player
    who is not playing or telling something to player who is not logged
    in).
    """
    def __init__(self, player_name, message):
        FicsCommandExecutionException.__init__(
            self, "Can not execute command on %s: %s" % (player_name, message))
        self.player_name = player_name
        self.message = message


class UnknownFicsCommand(FicsCommandExecutionException):
    """Raised on attempts to execute unknown command"""


class AmbiguousFicsCommand(UnknownFicsCommand):
    """Raised on attempts to execute command which is ambiguous"""


class BadFicsCommandParameters(FicsCommandExecutionException):
    """Various problems with invalid parameters given to fics command"""
    pass


class InsufficientPermissions(FicsCommandExecutionException):
    """
    We lack some (FICS) rights (some specific cases are distinguished
    as subclasses).
    """
    pass


class TrueAccountRequired(InsufficientPermissions):
    """
    We are logged as guest, but try to do something only true users can.
    """
    pass


class TDAccountRequired(InsufficientPermissions):
    """
    We are normal user, but try to exec TD command.
    """
    pass


class FicsProtocolError(FicsCommandExecutionException):
    """
    Important violations of FICS protocol.
    """
    pass


class MissingBlockMarkers(FicsProtocolError):
    """
    Attempt to execute command without numeric prefix while working in block mode.
    """
    pass


class Disconnected(FicsProtocolError):
    """
    Thrown in case we are not currently connected, or at least not logged in, but
    the program attempts to call the function which require connected object.
    """
    pass


class BadFicsCommandSyntax(FicsProtocolError):
    """
    Thrown on missing command id, too long command etc.
    """
    pass


class AbstractMethodCalled(LibraryUsageException):
    """
    Virtual methods which must be implemented throw this exception.
    """
    pass


class PasswordPromptOnGuestConnection(FicsProtocolError):
    """
    While logging as guest, we got password prompt.  Most likely it
    means somebody registered the name we wanted to use as guest name.
    """
    pass


class FicsCommandTooLong(FicsCommandException):
    """Some command issued is definitely too long"""
    def __init__(self, command):
        FicsCommandException.__init__(self)
        FicsCommandException("Command too long: %s" % str(command))


