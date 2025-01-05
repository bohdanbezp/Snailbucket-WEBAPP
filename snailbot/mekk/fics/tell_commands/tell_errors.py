# -*- coding: utf-8 -*-

"""
Exceptions thrown from tell-command framework
"""

from mekk.fics.errors import FicsClientException, LibraryUsageException, InteractionException


# configuration-related errors

class ShortcutAliasToUnknownKeyword(LibraryUsageException):
    """ShortcutResolver config exception: attempt to alias non-existant keyword"""

class ShortcutKeywordConflict(LibraryUsageException):
    """ShortcutResolve config exception: duplicate alias/keyword"""

# Shortcut-resolving stage

class ShortcutResolvingError(InteractionException):
    """Could not resolve keyword"""
    user_msg = "Bad command name or parameters"

class ShortcutAmbiguousKeyword(ShortcutResolvingError):
    """
    Many resolutions possible
    :param given: string specified (for example "li")
    :type given: str
    :param available: strings possible (for example ["list", "link"])
    :type available: [str]
    """

    def __init__(self, given, available):
        self.bad_keyword = given
        self.matching_keywords = available
        self.user_msg = "Ambiguous word: %s (matching all of: %s)" % (
            self.bad_keyword, ", ".join(sorted(self.matching_keywords)))

class ShortcutUnknownKeyword(ShortcutResolvingError):
    def __init__(self, bad_keyword, known_keywords):
        self.bad_keyword = bad_keyword
        self.known_keywords = known_keywords
        self.user_msg = "Unknown word: %s (expected one of: %s)" % (
            bad_keyword, ", ".join(sorted(known_keywords)))

# Improved errors for true tell command parsing

class InvalidTellCommand(FicsClientException):
    """
    Base class for exceptions thrown in case of tell command parsing errors
    (bad commands, bad parameters etc)
    """
    user_msg = "I do not understand your command."

class AmbiguousCommand(InvalidTellCommand):
    """
    Thrown when given shortcut is non unique (for example we have
    both "listgames" and "listplayers", and somebody issued "list"
    """
    def __init__(self, given_command, matching_commands_list):
        self.command = given_command
        self.matching_keywords = matching_commands_list
        self.user_msg = "Ambiguous command: %s. Which do you mean: %s?" % (
            given_command, ",".join(sorted(matching_commands_list)))

class EmptyCommand(InvalidTellCommand):
    """
    Thrown on attempt to execute empty command
    """
    user_msg = "No command given"

class UnknownCommand(InvalidTellCommand):
    """Thrown when some command not matching anything is called"""

    def __init__(self, command, known_commands):
        self.bad_keyword = command
        self.known_keywords = known_commands
        self.user_msg = "Unknown command: %s (expected one of: %s)" % (
            command, ", ".join(sorted(known_commands)))

class InvalidCommandParameters(InvalidTellCommand):
    """General command parameters error."""
    user_msg = "Invalid command parameters"
    def __init__(self, command):
        self.command = command

class UnknownCommandParameter(InvalidCommandParameters):
    """Thrown when some unknown parameter is used."""
    def __init__(self, command, bad_param, known_params):
        super(UnknownCommandParameter, self).__init__(command)
        self.bad_param = bad_param
        self.known_params = known_params
        self.user_msg = "Unknown parameter: %s (expected one of: %s)" % (
            bad_param, ", ".join(sorted(known_params)))

class AmbiguousCommandParameter(InvalidCommandParameters):
    """Thrown when some param name is shortened too much"""
    def __init__(self, command, given_param, matching_params_list):
        super(InvalidCommandParameters, self).__init__(command)
        self.given_param = given_param
        self.matching_params = matching_params_list
        self.user_msg = "Ambiguous parameter %s. Which do you mean: %s?" % (
            given_param, ",".join(sorted(matching_params_list)))

class BadFreeParametersCount(InvalidCommandParameters):
    """Thrown when there are too many or not enough non-named params"""
    def __init__(self, command, given, min, max):
        """ Good is min <= given < max
        """
        super(BadFreeParametersCount, self).__init__(command)
        self.min_count = min
        self.max_count = max
        self.given_count = given
        if max == 0:
            self.user_msg = "Got unexpected parameter"
            return
        if given == 0:
            got_no = "Got no params"
        else:
            got_no = "Got %d params" % given
        if min+1 == max:
            self.user_msg = "Invalid parameters. %s, expected %d" % (got_no, min)
        elif max is None:
            self.user_msg = "Invalid parameters. %s, expected at least %d" % (got_no, min)
        else:
            self.user_msg = "Invalid parameters. %s, expected at from %d to %d" % (got_no, min, max-1)
