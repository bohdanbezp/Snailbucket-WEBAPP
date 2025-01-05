# -*- coding: utf-8 -*-

"""
Support for command  parsing, mainly used in TellCommandsMixin. Handles
shortcuts and syntax verificatin.
"""

import re, logging
from mekk.fics.tell_commands.shortcut_resolver import ShortcutResolver
from mekk.fics.tell_commands import tell_errors
#from mekk.fics.tell_commands.odict import OrderedDict

logger = logging.getLogger("fics.lib")

re_spaces = re.compile(r'[\s\r\n]+')
re_eq=re.compile(r'^(?P<name>[^=]+)=(?P<value>.*)$')

class CommandParser(object):
    """
    Handle expressions of type

        command par=sth par2=sthelse nonkeywordparam nonkeywordparam2

    Commands, and optionally params, can be shortened, or accessed by aliases.

    This class resolves such strings and returns canonical command name,
    dictionary of named params, and list of remaining params.

    """

    def __init__(self):
        self.command_resolver = ShortcutResolver()
        self.params_resolvers = dict()
        self.min_positional_params_count = dict()
        self.max_positional_params_count = dict()

    def register_command(self, command_name, command_aliases=None,
                         param_names=None, param_aliases=None,
                         min_positional_params_count=0,
                         max_positional_params_count=None):
        """
        Registers given command, with list of aliased names.

        Optionally allows to define param_names. If specified, named params
        are resolved against this list. If not, any params will be forwarded
        as-is.

        :param command_name: name of registered command (for example "list")
        :param command_aliases: list of alternative command names (like ["show", "print"]), empty by default
        :param param_names: list of known named params. If not given, params won't be resolved but resolved as is, empty by default
        :param param_aliases: alternative names of params, mapping alias → canonical param name, empty by default
        :param min_positional_params_count: minimal expected number of positional params
        :param max_positional_params_count: maximal expected number of positional params
        """
        self.command_resolver.add_keyword(command_name)
        if command_aliases:
            for alias in command_aliases:
                self.command_resolver.add_alias(alias, command_name)
        if param_names:
            self.params_resolvers[command_name] = ShortcutResolver(
                keywords=param_names, aliases=param_aliases
            )
        self.min_positional_params_count[command_name]=min_positional_params_count
        self.max_positional_params_count[command_name]=max_positional_params_count

    def parse_command(self, text):
        """
        Parse given text. In case it can not be parsed,
        throws exception.

        :returns: command name, args, kwargs (the latter is a list of (key, value) pairs,
           it is up to the user to decide whether it should be converted to dict, multidict,
           ordereddict, orderedmultidict or anything else)
        """
        words = re_spaces.split(text.strip(' \r\n'))

        if not words:
            raise tell_errors.EmptyCommand()

        try:
            command_name = self.command_resolver.resolve(words.pop(0))
        except tell_errors.ShortcutAmbiguousKeyword as e:
            raise tell_errors.AmbiguousCommand(e.bad_keyword, e.matching_keywords)
        except tell_errors.ShortcutUnknownKeyword as e:
            raise tell_errors.UnknownCommand(e.bad_keyword, e.known_keywords)

        args = []
        #kwargs = OrderedDict()
        kwargs = []
        for word in words:
            m = re_eq.search(word)
            if m:
                name = m.group('name')
                value = m.group('value')
                if command_name in self.params_resolvers:
                    # Param names defined, so we can resolve using them
                    try:
                        name=self.params_resolvers[command_name].resolve(name)
                    except tell_errors.ShortcutAmbiguousKeyword as e:
                        raise tell_errors.AmbiguousCommandParameter(
                            command_name, e.bad_keyword, e.matching_keywords)
                    except tell_errors.ShortcutUnknownKeyword as e:
                        raise tell_errors.UnknownCommandParameter(
                            command_name, e.bad_keyword, e.known_keywords)
                #kwargs[name] = value
                kwargs.append( (name, value) )
            else:
                args.append(word)
        pos_min = self.min_positional_params_count[command_name]
        pos_max = self.max_positional_params_count[command_name]
        if len(args) < pos_min:
            raise tell_errors.BadFreeParametersCount(
                command_name, len(args), pos_min, pos_max)
        if pos_max is not None:
            if len(args) > pos_max:
                raise tell_errors.BadFreeParametersCount(
                    command_name, len(args), pos_min, pos_max)
        return command_name, args, kwargs

    def resolve_command_name(self, command_name):
        """
        Tries to resolve command name only - converting it to the canonical form.
        Mostly for use in help handling.
        """
        try:
            return self.command_resolver.resolve(command_name)
        except tell_errors.ShortcutAmbiguousKeyword as e:
            raise tell_errors.AmbiguousCommand(e.bad_keyword, e.matching_keywords)
        except tell_errors.ShortcutUnknownKeyword as e:
            raise tell_errors.UnknownCommand(e.bad_keyword, e.known_keywords)

    def list_commands(self, skip_aliases=False):
        return self.command_resolver.list_keywords(skip_aliases=skip_aliases)

