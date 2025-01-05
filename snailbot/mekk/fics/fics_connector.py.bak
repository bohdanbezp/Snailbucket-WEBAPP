# -*- coding: utf-8 -*-

"""
Twisted-based FICS connection handling..

To use, instantiate FicsFactory (which internally uses FicsProtocol),
binding to it the appropriate FicsClient object (see examples).
"""
#from __future__ import unicode_literals
from __future__ import absolute_import
import six

from twisted.internet import protocol, defer, reactor, error as twisted_error
from twisted.protocols import basic

import logging
import traceback
from mekk.fics import errors
from mekk.fics.support.keep_alive import KeepAliveExecutor
from mekk.fics.support.block_deferreds import BlockDeferreds
from mekk.fics.support.delaying_executor import DelayingExecutor
from mekk.fics.parsing.login_parser import is_authorized_login_confirmation, is_guest_login_confirmation, is_password_prompt, is_login_prompt
from mekk.fics.parsing.fics_parser import FicsTextProcessor
from mekk.fics.support.list_cache import ListCache
from mekk.fics.command_building import ivar_login
from mekk.fics.twisted_util import defer_util

logger_net = logging.getLogger('fics.net')
logger_lib = logging.getLogger('fics.lib')

######################################################################

# TODO: activate periodical check for timed-out commands (block deferreds)


#noinspection PyClassicStyleClass
class FicsProtocol(basic.LineReceiver):
    """
    Handling details of actual FICS connection.

    This object is usually not used directly by bot/client code, it's
    services and callbacks are provided to it via the means of
    FicsClient class.
    """
    prompt = 'fics%'

    # The following attributes are setup during the life of the object,
    # here we define them to document the names and to avoid code analyzer
    # warnings.

    # Actual client object, set by the FicsFactory just after protocol creation
    client = None

    # Objects created after successful connection or after successful login

    # FICS handle under which we are connected. Note that it may
    # differ from given username - obviously in case of guest login (we specify
    # 'guest', but finish being 'GuestVXBZ' or so), but also in case of
    # registered login (at least upper/lower case may differ).
    #
    # The variable is also used to check whether we are connected, or not, as
    # it is cleared on disconnection.
    _fics_handle = None

    # Worker objects handling delegated aspects of the clas functionality
    _line_sender = None
    _reply_deferreds = None
    _keep_alive = None
    _list_cache = None

    ##################################################################
    # Helper properties
    ##################################################################

    @property
    def label(self):
        return self.client.label

    @property
    def fics_handle(self):
        if self._fics_handle:
            return self._fics_handle
        else:
            raise errors.Disconnected("Attempt to query FICS handle when we are not logged in")

    @property
    def ivars_to_set_on_login(self):
        """
        List of interface variables which must be set on login. This is extracted to
        separate routine as in the future we may provide hook to add some more (still,
        things like BLOCK or NOWRAP must always be set)
        """
        return [
            'BLOCK',        # command result wrapping
            'DEFPROMPT',    # force prompt 'fics% '  (or 'hh:mm_fics% ' if ptime is set)
            'NOWRAP',       # no line wrapping
            'STARTPOS',     # provide initial board before move list if initial position is not standard, http://www.freechess.org/Help/HelpFiles/iv_startpos.html
            'GRAPH',        # disables ASCII graphics in commands like ustat, generates parseable data instead, http://www.freechess.org/Help/HelpFiles/iv_graph.html
            #'ALLRESULTS'    # gin reports also adjudication results
        ] + self.client.interface_variables_to_set_after_login + [
            'LOCK',
        ]

        # !!! TODO: gameinfo http://www.freechess.org/Help/HelpFiles/iv_gameinfo.html

        # TODO: seeks in seekinfo mode
        # http://www.freechess.org/Help/HelpFiles/iv_seekinfo.html
        # http://www.freechess.org/Help/HelpFiles/iv_seekremove.html
        # iset seekremove 1
        # iset seekinfo 1
        # set seek 1

        # TODO: pendinfo: http://www.freechess.org/Help/HelpFiles/iv_pendinfo.html
        # (extra notes about pending offers)

        # TODO: consider finishing with iv_lock (forbids any iv_* changes until logout)

        # TODO: consider compressmove http://www.freechess.org/Help/HelpFiles/iv_compressmove.html

        # TODO: check those possible variables (so far described as not implemented)
        # - boardinfo http://www.freechess.org/Help/HelpFiles/iv_boardinfo.html
        # - extuserinfo http://www.freechess.org/Help/HelpFiles/iv_extuserinfo.html
        # - extascii http://www.freechess.org/Help/HelpFiles/iv_extascii.html

    @property
    def variables_to_set_after_login(self):
        """
        List of FICS variables which must be set (after login) to make the object fully
        functional. Can be extended or partially overridden by the client object.
        Defaults are rather restrictive (for example disable shouts, gin, pin, game offers etc),
        set so with the intent to limit unnecessary communication. Enable whatever your code
        needs
        """
        variables = {
            'style': 12,
            'interface': 'mekk.fics (HorriblyMessedUpPythonCode)',
            'width': 240,
            'open': 0,
            'highlight': 0,
            'shout': 0,
            'cshout': 0,
            'seek': 0,
            'tell': 1,       # Listen to guests
            'tolerance': 5,  # No bad words filtering
            'gin': 0,
            'pin': 0,
            'kibitz': 1,
            'unobserve': 3,  # Observe to the very end if we start to
        }
        vars_by_user = self.client.variables_to_set_after_login
        for var, value in six.iteritems(vars_by_user):
            variables[var] = value
        return variables

    ##################################################################
    # Virtual methods implemented in subclasses (different on true
    # connection and on the guest level)
    ##################################################################

    def provide_auth_username(self):
        """
        Which username to give during login?
        """
        raise errors.AbstractMethodCalled()

    def provide_auth_password(self):
        """
        Which username to give during login?
        """
        raise errors.AbstractMethodCalled()

    def check_if_logged(self, data):
        """
        Checks whether given text proves successful login. If so, returns
        the username assigned, if not, returns None. Used in login stage
        processing.
        :param data: Reply text obtained after login
        """
        raise errors.AbstractMethodCalled()

    def is_guest(self):
        """
        Am I a guest?
        """
        raise errors.AbstractMethodCalled()

    def is_TD(self):
        """
        Am I TD?
        """
        raise errors.AbstractMethodCalled("is_TD")

    ##################################################################
    # Implementation of Twistedish virtual methods
    ##################################################################

    # Note: we use raw/line mode to distinguish the login phase from the fun phase.
    # While logging in we remain in raw mode, later on we switch to line one.
    # This way we do not need to make ifs in every callback.

    def connectionMade(self):
        """
        Called on new connection (including reconnected).
        """
        logger_net.debug("%s: Made TCP connection to the server" % self.label)
        self.delimiter = six.b("\n\r")
        self._fics_handle = None
        self._keep_alive = None
        self._input_processor = FicsTextProcessor(
            info_callback=self.on_fics_information,
            block_callback=self.on_command_reply,
            label=self.label,
            #fics_prompt=self.prompt,
        )
        self._reply_deferreds = BlockDeferreds(label=self.label)
        self._line_sender = DelayingExecutor(
            interval = self.client.min_interval_between_commands,
            command = self.sendLine,
            label = self.label)
        self._list_cache = ListCache(label=self.label, run_command=self.run_command)
        self.transport.setTcpNoDelay(self.client.disable_nagle)
        self.setRawMode()

    def connectionLost(self, reason=protocol.connectionDone):
        logger_net.info("%s: TCP connection lost: %s" % (self.label, str(reason)))
        self._fics_handle = None
        self._reply_deferreds = None
        self._line_sender = None
        self._keep_alive = None  # This also stops
        self._list_cache = None
        d = self.client.on_logout()
        if isinstance(d, defer.Deferred):
            d.addErrback(self.on_unhandled_processing_failure)
        return basic.LineReceiver.connectionLost(self, reason)

    def rawDataReceived(self, data):
        if six.PY3:
            if isinstance(data, six.binary_type):
                data = data.decode('latin-1')
        logger_net.debug("%s: Receive(raw): %s" % (self.label, data))
        if is_login_prompt(data):
            iv = self.ivars_to_set_on_login
            if iv:
                self.sendLine(ivar_login.ivar_login_line(*iv))
            self.sendLine(self.provide_auth_username())
            return
        if is_password_prompt(data):
            self.sendLine(self.provide_auth_password(), hide_text_in_logging=True)
            return
        name = self.check_if_logged(data)
        if name:
            self._fics_handle = name
            logger_lib.info("%s: Logged in as %s" % (self.label, name))
            self.setLineMode()
            d = self.post_login()
            if isinstance(d, defer.Deferred):
                d.addErrback(self.on_unhandled_processing_failure)
            else:
                logger_lib.warn("%s: Ugly object returned from post_login method (expected Deferred): %s" % (
                    self.label, str(d)))

    def lineReceived(self, line):
        # Note: we split most of lineReceived into doLineReceived to make it possible
        # to return results deferred from doLineReceived (due to some obscure Twisted convention,
        # if anything "true" is returned from lineReceived, Twisted breaks the connection).
        #
        # Here we call that method and handle errors (if any).
        if not isinstance(line, six.string_types):
            line = line.decode("latin-1")
        d = self.doLineReceived(line)
        if d:
            if isinstance(d, defer.Deferred):
                d.addErrback(self.on_unhandled_processing_failure)
            else:
                logger_lib.warn("%s: Ugly object returned from input handling method (expected Deferred): %s" % (
                    self.label, str(d)))
        else:
            logger_lib.warn("%s: No reply from input handling method while handling line '%s' (consider returning deferred to signal finished processing)" % (
                self.label, line))

    def doLineReceived(self, line):
        """
        Called on every line got from FICS. Returns deferred fired with the results of callback
        processing (this is mekk.fics convention by which we make it able to wait for finish
        of such processing, at least in unit-tests, and to capture unhandled errors).
        """
        # TODO: move logging to input_processor
        logger_net.debug("%s: Receive(line): %s" % (self.label, line))
        sync_list = self._input_processor.consume_input_line(line)
        defs_list = []
        for item in sync_list:
            if isinstance(item, defer.Deferred):
                defs_list.append(item)
            elif item is None:
                logger_lib.warn("%s: No reply from (some) input handling method while handling line '%s' (consider returning deferred to signal finished processing)" % (
                    self.label, line))
            else:
                logger_lib.warn("%s: Ugly object returned from (some) input handling method (expected Deferred) while handling line '%s': %s" %(
                    self.label, line, str(item)))
        return defer_util.gather_with_cancel(defs_list)

    #noinspection PyMethodOverriding
    def sendLine(self, line, hide_text_in_logging=False):
        """
        Default sendLine overridden to provide debug logging and type
        coercions.
        """
        if not hide_text_in_logging:
            logger_net.debug("%s: Send: %s" % (self.label, line))
        else:
            logger_net.debug("%s: Send: ****hidden****" % self.label)
        if not isinstance(line, six.binary_type):
            line = six.b(line)
        basic.LineReceiver.sendLine(self, line)

    ##################################################################
    # Methods used by client object
    ##################################################################

    def run_command(self, command):
        """
        Execute given (as text) FICS command. Returns deferred which will
        be fired once the command is executed (or errbacked if it fails).

        Internally uses block-mode methods to match reply to the command.

        :param command: command text (for example "finger john")
        :returns: deferred firef once command results are known
        :rtype: defer.Deferred
        """
        d = self.run_command_ext(command)
        def strip_name(val):
            return val[1]   # val is name, reply we need only reply here
        d.addCallback(strip_name)
        return d

    def run_command_ext(self, command):
        if type(command) is str:
            command = str(command)
        if not self._reply_deferreds:
            #raise errors.Disconnected("Attempt to run FICS command while we are not logged in")
            return defer.fail(
                errors.Disconnected("Attempt to run FICS command while we are not logged in") )
        id, d = self._reply_deferreds.allocate()
        line_to_send = '%d %s' % (id, command)
        self._line_sender.execute(line_to_send)
        return d

    def fics_list_items(self, list_name, max_age_in_seconds,
                        treat_as_players=False, treat_as_ints=False):
        """
        Returns contents of some FICS list (like TD or computers),
        cache the results for given time to avoid reloads.

        If treat_as_players is set, returned object is set of PlayerName objects
        (ensuring case-less comparisons and easy belonging checking).

        If treat_as_ints is set, returned object is a set of int's (useful for example
        for the list of subscribed channel).

        Otherwise plain list of strings is returned
        """
        return self._list_cache.get_items(
            list_name, max_age_in_seconds,
            treat_as_players=treat_as_players, treat_as_ints=treat_as_ints)

    ##################################################################
    # Helpers/support methods
    ##################################################################

    @defer.inlineCallbacks
    def post_login(self):
        """
        Code run after successful login.

        Configures the connections (sets necessary FICS variables), then
        notifies the actual client that the connection is ready to be used.

        Returns deferred of unimportant value, which is fired once all executed
        operations are finished.
        """
        try:
            logger_lib.debug("%s: logged in as %s" % (self.label, self._fics_handle))

            # Gathering replies so requests can proceed in parallel
            pending_defs = []

            vs = self.variables_to_set_after_login
            for var, value in six.iteritems(vs):
                pending_defs.append(
                    self.run_command("set %s %s" % (var, str(value))))
            # TODO: virtualize somehow channel subscriptions
            pending_defs.append(
                self.run_command("- channel 53"))
            if self.client.use_keep_alive:
                self._keep_alive = KeepAliveExecutor(
                    label=self.label,
                    frequency=self.client.keep_alive_frequency,
                    timeout=self.client.keep_alive_timeout,
                    command=lambda: self.run_command("date"),
                    on_failure=self.on_command_timeout)
            if self.client.command_execution_timeout:
                self._reply_deferreds.activate_timeout_checker(
                    check_frequency_in_seconds=self.client.command_execution_timeout / 3,
                    timeout_in_seconds=self.client.command_execution_timeout,
                    on_timeouts=self.on_command_timeout)

            # Await for configuration finish
            yield defer_util.gather_with_cancel([
                d for d in pending_defs if d])

            # Custom initialization
            yield self.client.on_login(self._fics_handle)

        except twisted_error.ConnectionDone as e:
            logger_lib.info("%s: Cleanly disconnected while handling on_login (%s). This is normal in one-shot clients." % (
                self.label, str(e)))
        except twisted_error.ConnectionClosed as e:
            logger_lib.warn("%s: Connection lost while handling on_login (%s). If this is done on purpose, consider syncing on client.disconnect() before stopping the reactor" % (
                self.label, str(e)))
        except Exception as e:
            logger_lib.error("%s: Failure while logging in. Stopping. Failure:\n" % self.label\
                + traceback.format_exc(e))
            if reactor.running:
                reactor.stop()

    def on_unhandled_processing_failure(self, failure):
        """
        Called on unhandled errors.
        """
        logger_lib.fatal(
            "%s: Unhandled error, stopping program." % self.label,
            exc_info=(
                failure.type,
                failure.value,
                failure.getTracebackObject()))
        #TODO: delegate to client so the user code can recover from this failure
        #logger_lib.error("%s: Unhandled error, stopping program. Error details:\n%s" % (
        #    self.label, err))
        if reactor.running:
            reactor.stop()

    def on_command_timeout(self):
        """
        Called whenever we detect that some command timed out (either by means of periodical run
        through active commands, or by keepalive object). Such case means frozen, or at least
        heavily lagging connection.

        Delegates handling to the identically named method of client object.
        """
        self.client.on_command_timeout()

    def on_fics_information(self, event_type, event_data):
        """
        Called whenever we get some out of bonds (non-reply) information from FICS
        """
        if event_type != "unknown":
            return self.client.on_fics_information(event_type, event_data)
        else:
            return self.client.on_fics_unknown(event_data.text)

    def on_command_reply(self, command_id, command_name, status, command_data):
        """
        Called whenever we get reply to some command
        """
        # TODO: consider using command_name
        if status:
            return self._reply_deferreds.fire(command_id, (command_name, command_data))
        else:
            return self._reply_deferreds.fire_error(command_id, command_data)


#noinspection PyClassicStyleClass
class FicsGuestProtocol(FicsProtocol):
    """
    Obsługa pracy w trybie gościa
    """

    def __init__(self, auth_guest_name='guest'):
        self._auth_guest_name = auth_guest_name

    def provide_auth_username(self):
        return self._auth_guest_name

    def provide_auth_password(self):
        raise errors.PasswordPromptOnGuestConnection(
            failure_reason="Unexpected password prompt on guest connection")

    def check_if_logged(self, data):
        guest_name = is_guest_login_confirmation(data)
        if guest_name:
            self.sendLine("")
            return guest_name

    def is_guest(self):
        return True

    def is_TD(self):
        return defer.succeed(False)


#noinspection PyClassicStyleClass
class FicsUserProtocol(FicsProtocol):
    """
    Obsługa pracy w trybie użytkownika zalogowanego
    """
    def __init__(self, auth_username, auth_password):
        self._auth_username = auth_username
        self._auth_password = auth_password
        if not self._auth_password:
            raise errors.LibraryUsageException("Attempt to use empty password")
        self._is_TD = None

    def provide_auth_username(self):
        return self._auth_username

    def provide_auth_password(self):
        return self._auth_password

    def check_if_logged(self, data):
        return is_authorized_login_confirmation(data)

    def is_guest(self):
        return False

    def is_TD(self):
        if self._is_TD is not None:
            return defer.succeed(self._is_TD)
        d = self.fics_list_items("td", self.client.public_list_cache_age, treat_as_players=True)
        def return_info(items):
            self._is_TD = self.fics_handle in items
            return self._is_TD
        d.addCallback(return_info)
        return d

######################################################################

# Note: Twisted, for backward-compatibility reasons, uses old-style
# classes. Therefore mixin below is old-style, and we need some hacks
# to replace non-existing super.

# TODO: there is still some code duplication between factories.
# Check whether playing with super may help, or maybe constant
# with superclass.

# old style class on purpose as it is mixed with old style classes
#noinspection PyClassicStyleClass
class FicsFactoryMixIn:
    """
    Common methods shared between FicsFactory and ReconnectingFicsFactory.
    This class is not useful by itself.
    """

    # Note: both FicsFactory, and ReconnectingFicsFactory set 
    # 'client_factory_cls' class attribute to proper parent class
    # (protocol.ClientFactory or protocol.ReconnectingClientFactory).
    # This allows us to implement clientConnection(Failed|Lost) once
    # in spite of lack of super in Twisted old-style classes.
    #client_factory_cls = None

    def __init__(self, client, auth_username, auth_password):
        self.client = client
        self.auth_username = auth_username
        self.auth_password = auth_password
        #
        self._disconnect_callbacks = []
        
    @property
    def label(self):
        return self.client.label

    def notify_on_disconnect(self, callback):
        """
        Will fire given callback on (first future) connection lost/failed. Used by Client.disconnect
        to provide working deferred.
        """
        logger_lib.debug("%s: Registering disconnect callback: %s" % (self.label, callback))
        self._disconnect_callbacks.append(callback)

    def _notify_about_disconnect(self, reason):
        logger_lib.debug("Disconnect reported, reason %s" % reason)
        to_fire = self._disconnect_callbacks
        self._disconnect_callbacks = []
        for callback in to_fire:
            logger_lib.debug("%s: Firing disconnect callback: %s" % (self.label, callback))
            #callback(reason) # impossible as triggers deferred error handling
            callback(None)

    #noinspection PyUnusedLocal
    def startedConnecting(self, connector):
        logger_net.debug("%s: Initiated connection attempts" % self.label)

    def buildProtocol(self, addr):
        logger_net.info("%s: TCP-connected to the server at %s" % (self.label, addr))
        if self.auth_password:
            my_protocol = FicsUserProtocol(self.auth_username, self.auth_password)
        else:
            my_protocol = FicsGuestProtocol(self.auth_username)
        my_protocol.client = self.client
        # TODO: consider checking whether those attributes are present and warning
        # or even failing (client object won't work on two factories simultaneously,
        # the latter will win). But remember about reconnecting factory with still
        # the same client object
        self.client.protocol = my_protocol
        self.client.factory = self
        return my_protocol

    def clientConnectionFailed(self, connector, reason):
        #import twisted.python.failure
        #assert(isinstance(reason,twisted.python.failure.Failure))
        try:
            logger_net.warn("%s: TCP connection attempt failed. Reason: %s" % (self.label, reason.getErrorMessage()))
            self.client_factory_cls.clientConnectionFailed(self, connector, reason)
        finally:
            self._notify_about_disconnect(reason)

    def clientConnectionLost(self, connector, reason):
        try:
            # possible reasons: twisted.internet.error.ConnectionLost or twisted.internet.error.ConnectionDone
            if reason.check(twisted_error.ConnectionDone):
                logger_net.info("%s: TCP connection closed cleanly on our demand, reason: %s" % (self.label, reason))
            else:
                logger_net.warn("%s: TCP connection lost. Reason: %s (%s)\nTraceback: %s" % (
                        self.label, reason.getErrorMessage(), reason.value.__class__.__name__, reason.getTraceback()))
            self.client_factory_cls.clientConnectionLost(self, connector, reason)
        finally:
            self._notify_about_disconnect(reason)

#noinspection PyClassicStyleClass
class FicsFactory(FicsFactoryMixIn, protocol.ClientFactory):
    """
    Factory for one-shot (not-reconnecting) bots.
    """

    client_factory_cls = protocol.ClientFactory

    def __init__(self, client, auth_username='guest', auth_password=''):
        FicsFactoryMixIn.__init__(self, client, auth_username, auth_password)
        #protocol.ClientFactory.__init__(self)


#noinspection PyClassicStyleClass
class ReconnectingFicsFactory(FicsFactoryMixIn, protocol.ReconnectingClientFactory):
    """
    Factory for long-running bots, reconnects in case of failure.

    Note that it requires some care (cleanup etc) in client code, if it
    is troublesome, use FicsFactory and restart whole process when it fails
    using tools like upstart, supervised, runit or similar.
    """

    client_factory_cls = protocol.ReconnectingClientFactory

    def __init__(self, client, auth_username='guest', auth_password=''):
        FicsFactoryMixIn.__init__(self, client, auth_username, auth_password)
        #protocol.ReconnectingClientFactory.__init__(self)
        self.maxDelay = 180 # domyślna godzina 3600 to za dużo
        self.noisy = True   # by log twisted logował info o przełączeniach

    def buildProtocol(self, addr):
        self.resetDelay()  # reconnection delay
        return FicsFactoryMixIn.buildProtocol(self, addr)



