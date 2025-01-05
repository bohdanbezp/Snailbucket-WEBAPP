# -*- coding: utf-8 -*-

"""
Framework for handling tells directed to the bot. Commands are defined
as command objects and registered in a bot/client.
"""

from twisted.internet import defer
from mekk.fics import errors

import logging
from mekk.fics.tell_commands.command_parser import CommandParser
from mekk.fics.support.tell_status import TellLoopPrevention

logger = logging.getLogger("fics.lib")

class TellCommand(object):
    """
    Base class for commands registered in TellCommandMixin. Defines
    the object interface.

    Single object instance is reused for many commands, therefore no
    user-related attributes should be kept on the instance level.
    """

    #######################################################################
    # Specification
    #######################################################################

    @classmethod
    def name(cls):
        """
        Returns the command name, as used by players talking to the bot.

        The default implementation handles two frequent conventions:

        - if class name starts with Cmd, strips this prefix, lowercases and returns
          the rest

        - if class name ends with Command, strips this suffix, lowercases and returns
          the rest

        - otherwise just lowercases the class name.

        So, for example, class CmdObserve will be by default handling command "observe",
        "MailCommand" will handle "mail", and "ListGames" will handle "listgames".

        Of course, the method can be just overridden to support any other name.
        """
        n = cls.__name__
        if n.startswith("Cmd"):
            return n[3:].lower()
        elif n.endswith("Command"):
            return n[:-7].lower()
        else:
            return n.lower()

    @classmethod
    def name_aliases(cls):
        """
        Provides alternative names for the command (so, for example, "listgames"
        can be also available as "lg"). All aliases should be returned as a list.

        By default the list is empty, there are no aliases.
        """
        return []

    @classmethod
    def named_parameters(cls):
        """
        Returns the list of all supported named parameters. There are 3 possibilities:

        - if this method returns None (that's default), any named params are supported as-is,
          whetever client set, is sent to the run method,
        - if this method returns list of names, all user-specified named params are matched
          against this list, including support for using shortened/truncated versions, unknown
          params or non-unique are treated as errors
        - if this method returns empty list, named params are not supported and treated as errors,
        """
        return None

    @classmethod
    def positional_parameters_count(cls):
        """
        Defines how many positional parameters should the command receive. This method
        should return pair [min,max) - the command is expected to get >= min and < max
        params. None can be used as max to mark there is no upper limit.

        By default method returns (0, None) what means any number of params.
        """
        return 0, None

    #######################################################################
    # Execution
    #######################################################################

    def run(self, client, player, args, named_args):
        """
        Called to execute the command. This method must be implemented, default throws
        an exception.

        Handles the command according to it's specifics, returns the text which
        is to be told to the issuer (usually some confirmation/result,
        if there is no need to tell anything, None can be returned).

        Errors, if any, should be reported as exceptions. Would
        exception happen, there are a few ways in which user-targeted
        messages may be defined, in order:
        
        - method exception_text_for_user of TellCommandMixin is called and
          can be overriden on bot level in case special ways of mapping exceptions
          are needed

        - it's default implementation checks the exception object
          for attribute 'user_msg' or (if it is absent), method
          'format_user_msg', finally uses some default error text

        :param client: client object (which can be used to issue FICS commands,
            and possibly bot/client code methods)
        :type client: FicsClient
        :param player: who issued the command?
        :type player: PlayerType
        :param args: list of positional arguments (guaranteed to satisfy positional_parameters_count requirement)
        :type args: [str]
        :param named_args: keyword arguments (resolved against list of known params - if specified
            by named_parameters)
        :type named_args: OrderedDict
        :return: deferred signalling end of processing, or string if it is over immediately. If string is non-empty (or
            deferred is fired with non-empty string as parameter), the string is treated as result to be told to the player issuing
            the command
        :rtype: defer.Deferred or str
        """
        raise errors.AbstractMethodCalled()

    def help(self, client):
        """
        Called whenever short command help text is needed
        :param client: client object (which can be used to issue FICS commands,
            and possibly bot/client code methods)
        :type client: FicsClient
        """
        raise errors.AbstractMethodCalled()

#    # TODO: help
#    # TODO: handling failures
#    def _handle_failure(self, failure, who, context):
#        assert(isinstance(failure, twisted.python.failure.Failure))
#        assert(isinstance(context, CommandContext))
#        tmpl, msg, args = (None, None, None)
#        if failure.check(*all_errors):
#            tmpl = failure.value.name()
#            msg = failure.getErrorMessage()
#            args = failure.value.args()
#            args[ 'command' ] = self.name()
#        else:
#            tmpl = 'err'
#            msg = failure.getErrorMessage()
#            #logger.warn(msg + failure.getBriefTraceback())
#            logger.warn(msg + failure.getTraceback())
#            args = {}
#        if WatchConfig.ERROR_DIAGNOSTIC_IN_TELL:
#            args['extra_info'] = msg
#        logger.info(msg + failure.getBriefTraceback())
#        context.getTellMaker().templatedShortTell(who, tmpl, args)

def make_tell_command(callable,
                      name,
                      name_aliases = None,
                      named_parameters=None,
                      min_positional_parameter_count = 0,
                      max_positional_parameter_count = 0,
                      help_text = ""):
    """
    Creating tell command without subclassing.

    :param callable: function which will be called with arguments (client, player, args, named_args)
    """
    if not name_aliases:
        name_aliases = []
    if not named_parameters:
        named_parameters = []
    cls = type('DynCmd' + name, (object,), {
        'name': lambda self: name,
        'name_aliases': lambda self: name_aliases,
        'positional_parameters_count': lambda self: (min_positional_parameter_count, max_positional_parameter_count),
        'named_parameters': lambda self: named_parameters,
        'run': lambda self, client, player, args, named_args: callable(
            client, player, args, named_args),
        'help': lambda self, client: help_text,
        })
    return cls()

class TellCommandsMixin(object):
    """
    Mixin which augments FicsClient so handling commands received as tells
    is easier. Classes using this mixin, instead handling on_tell, define
    sets of named commands and provide code to execute them. This class
    parses received tells into command names and parameters, and calls
    appropriate callbacks.

    Tell syntax
    ------------

    All tells are expected to start from the command name (which can be
    shortened in usual FICS style), with space-separated
    optional extra parameters. Parameters of syntax "name=value", are treated as
    named params.

    So, for example, if there are commands "listplayers", "listgames", "listteams",
    "register", and "help", and some player writes on FICS:

        tell OurBot listp john anne count=10 sort=rating

    thix mixin will call listplayers callback giving "john", and "anne" as two
    positional parameters, and there will be two named parameters:
    count="10", and sort="rating".

    Shortening named parameter names can be also supported, see TellCommand
    class for all details.

    Class usage
    ------------

    To use: either inherit actual client from this mixin, FicsEventMethodsMixin,
    and FicsCient (in this order):

        class MyClient(TellCommandsMixin,FicsEventMethodsMixin,FicsClient):
            # ...

    or skip FicsEventMethodsMixin but call on_tell to handle received tells
    in your notification of on_fics_information:

        class MyClient(TellCommansMixin,FicsClient):
            # ...
            def on_fics_information(self, what, params):
                if what == "tell":
                    return self.on_tell(params)
                # ...

    Setup all commands by calling register_command for all commands handled
    (in constructor or at some similar initialization stage).
    """

    def _ensure_initialized(self):
        """
        Object initialization. Called from first register_command
        (__init__ is not used to make things simpler in context
        of multiple inheritance)
        """
        if not hasattr(self, "_command_parser"):
            self._command_parser = CommandParser()
            self._commands = dict()
            self._tell_loop_prevention = TellLoopPrevention(max_errors_allowed=3)

    def register_command(self, command):
        """
        Registers some command.

        :param command: appropriate object (which specifies command name and params
            and provides a way to run it)
        :type command: TellCommand
        """
        self._ensure_initialized()
        name=command.name()
        minargs,maxargs=command.positional_parameters_count()
        self._commands[name]=command
        self._command_parser.register_command(
            command_name=name, command_aliases=command.name_aliases(),
            param_names=command.named_parameters(),
            min_positional_params_count=minargs,
            max_positional_params_count=maxargs)

    def command_names(self, include_aliases=False):
        """
        Returns list of names of all registered commands.
        Typical usage: generating help text
        :param include_aliases: Should alternative command names be returned too?
        :return: names of commands handled
        :rtype: [str]
        """
        return self._command_parser.list_commands(skip_aliases=(not include_aliases))

    def command_help(self, command_name):
        """
        Returns whatever given command .help method returns. If command of such name does not exist,
        raises UnknownCommand exception.
        :param command_name: full or partial name of the command
        """
        cmd = self._commands.get( self._command_parser.resolve_command_name(command_name) )
        return cmd.help(self)

    def exception_text_for_user(self, exc, stage, command_name, command_text):
        """
        Method called whenever some exception happens
        while user command is handled, responsible for
        defining the text which should be told to the 
        user.

        Can be overriden. Default implementation 
        probes exception object for user-targeted information:

        - if the exception has an attribute `user_msg`, it will be the
          error text presented to the user

        - if the exception has parameterless method `format_user_msg`,
          it's output will be told

        - otherwise general information about failure will be told.

        :param exc: actual exception
        :type exc: Exception
        :param stage: processing stage on which error happened - 'parse' if failure
            happened during parsing (error is about wrong command, wrong params etc),
            'run' if during actual processing
        :type stage: str
        :param command_name: name of the command, in run stage always known, in parse
            stage sometimes wrong or completely missin
        :type command_name: str
        :param command_text: full text of the command ("something a=1 x=2 blah bleh")
        :type command_text: str
        :returns: string to be told
        :rtype: str
        """
        if hasattr(exc, 'user_msg'):
            return exc.user_msg
        elif hasattr(exc, 'format_user_msg'):
            return exc.format_user_msg()
        elif stage == "parse":
            return "Bad command name or bad parameters."
        else:
            return "The command %s which you issued failed. If the problem persists, contact the bot admin." % command_name

    @defer.inlineCallbacks
    def on_tell(self, tell_info):
        """
        Called whenever we receive some direct tell.

        Parses the tell as command with arguments, matches it
        to the defined commands list, if appropriate command is found,
        calls it.

        Automatically applies prevention against tell loops.

        :param tell_info: info who said what (info.player, info.text)
        :type tell_info: AttributedTell
        :return: deferred fired once processing is finished (or errbacked when it fails)
        """
        # Permission stage
        if not (yield self.allow_player(tell_info.player)):
            return

        # Parsing stage
        command = None
        try:
            command, args, named_args = self._command_parser.parse_command(
                tell_info.text)
            self._tell_loop_prevention.good_tell(tell_info.player)
        except Exception as e:
            if self._tell_loop_prevention.bad_tell(tell_info.player):
                #self._fics_tell_maker.templatedShortTell(who, 'err_bad_command')
                yield self.tell_to(tell_info.player,
                    self.exception_text_for_user(e, 'parse', command, tell_info.text))
                logger.warn("Bad tell from %s: %s" % (tell_info.player, tell_info.text))
            else:
                logger.warn("Many successive bad tells from %s, last: %s" % (tell_info.player, tell_info.text))
            return
        # Execution stage

        try:
            logger.info("Executing %s command: %s" % (tell_info.player, tell_info.text))
            reply = yield defer.maybeDeferred(
                self._commands[command].run,
                self, tell_info.player, args, named_args)
            if reply:
                yield self.tell_to(tell_info.player, reply)
        except Exception as e:
            logger.error("Exception while executing %s command %s: %s" % (
                tell_info.player, tell_info.text, str(e)), exc_info=True)
            yield self.tell_to(tell_info.player,
                self.exception_text_for_user(e, "run", command, tell_info.text))

    @defer.inlineCallbacks
    def allow_player(self, player):
        """
        Check whether we handle commands from given player.
        This is easy hook for global-level whitelisting or
        blacklisting.

        If this method returns True (as deferred - async processing,
        like executing FICS command, is possible), we will continue
        to parsing and executing the command. If it returns False, tell of
        the player will be completely and silently ignored.

        By default we ignore TDs and computer accounts.

        :param player: player to check
        :type player: PlayerType
        :return: True if player's command should be executed, False otherwise
        :rtype: bool
        """
        # Ignore chat from RoboAdmin, Mamer, and possibly computer accounts.
        # Note yields - .is_TD and .is_computer are deferred-returning methods
        is_td = yield self.is_TD(player)
        if is_td:
            logger.info("Ignoring tell from TD %s" % player)
            defer.returnValue(False)
        is_computer = yield self.is_computer(player)
        if is_computer:
            logger.info("Ignoring tell from computer %s" % player)
            defer.returnValue(False)
        defer.returnValue(True)

