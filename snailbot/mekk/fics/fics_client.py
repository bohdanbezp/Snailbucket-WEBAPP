# -*- coding: utf-8 -*-

"""
Basic FICS connection handling.
"""

from twisted.internet import defer, reactor
from mekk.fics import errors
from mekk.fics.constants.limits import MAX_COMMAND_SIZE
import logging

logger=logging.getLogger("fics.lib")

class FicsClient(object):
    """
    Main hook for the actual code of the FICS bot/client. The client object
    is bound to actual FICS connection (by being given as parameter to appropriate
    FicsFactory), and:

    - is notified whenever anything important happens, be it succesfull login,
      somebody's tell, move in game played or observed, or anything else
      (appropriate handling code is implemented by overriding virtual methods),

    - provides ways to issue FICS commands, both on general level (run_command),
      and via specific methods in case of more frequent operations,

    - provides some information about the connection (starting from the FICS handle),

    - let's one fine-tune some configuration parameters

    Actual bots/clients derive from this class, sometimes adding one or more of
    the mixins.
    """
    # TODO: mention mixin names

    def __init__(self, label):
        self.label = label
        # Those two will be set by factory when the client is connected to the appropriate
        # objects
        self.protocol = None
        self.factory = None

    ################################################################################
    # Connection management
    ################################################################################

    def disconnect(self):
        """
        Disconnect from FICS. Note that in case ReconnectingFicsFactory is used,
        it will initiate new connection attempts.

        :return: deferred fired after actual disconnect (note: it won't fire if we are
                 already disconnected, sorry)
        """
        # TODO: think how to make it work when we we are disconnected
        # Workaround to provide working deferred in spite of Twisted lack of such one.
        d = defer.Deferred()
        self.factory.notify_on_disconnect(d.callback)
        # This unfortunately does not return anything
        self.protocol.transport.loseConnection()
        return d

    ################################################################################
    # Configuration hooks
    ################################################################################

    variables_to_set_after_login = {}
    """
    Dictionary of FICS variables which should be set immediately after login.

    The core library code sets some variables (for example
    set seek 0, set shout 0, set style 12, ...), here it
    is possible to modify them and to add those not handled there.
    The effect is similar to just setting the variable in on_login,
    but slightly faster (we avoid having library core setting some variable just to
    be changed by bot code a second later).

    Note: don't change `style`, `highlight`, `prompt`, `ptime`, or `Lang` (if it were
    implemented) unless you really know what you are doing. PLENTY of things will break!

    Use full lowercased names for variables.

    Example (such code usually is put into __init__):

        self.variables_to_set_after_login = {
            'shout': 1,
            'cshout': 1,
            'tzone': 'EURCST',
        }
    """

    interface_variables_to_set_after_login = []
    """
    List of interface variables (specified by name) which should be set during login.

    The core library code sets some of them by default (like 'BLOCK' or 'NOWRAP'), here
    it is possible to activate a few more. Note that some may result in syntax changes
    not supported by parsing routines.

    For the list of all known names refer to mekk.fics.command_building.ivar_login
    module (in particular, IVAR_FLAGS variable defined there)

    Example (such code usually is put into __init__):

        self.interface_variables_to_set_after_login = [
            'ALLRESULTS', 'SUICIDE', 'CRAZYHOUSE', 'LOSERS'
        ]
    """

    use_keep_alive = False
    """
    Set to true if keep-alive pings should be used.

    Keep-alive pings execute some simple command every few minutes
    and serve two purposes:

    - avoid disconnections due to inactivity (both due to 60 minutes
      FICS timeout, and possible firewall/routers timeouts)

    - ensure any case of connection freeze is detected reasonably soon
      (not every network problem results in explicit disconnection, it
      happens that the connections seems working, but no messages are passed
      anymore)

    By default keep-alive is NOT used. Set it to true for long-running
    bots, especially those which may happen to have nothing to do
    for some time.
    """

    keep_alive_frequency = 10 * 60
    """
    How often keep-alive checks are to be executed (in seconds)?
    By default - every 10 minutes.
    """

    keep_alive_timeout = 60
    """
    How long (in seconds) do we wait for keepalive command reply before
    assuming it does not work? This timeout may be shorter than
    command_execution_timeout as the latter must accomodate
    complicated commands.
    By default - 60 seconds.
    """

    command_execution_timeout = 2 * 60
    """
    Max time allowed for FICS command execution (in seconds).

    If within such time we do not get reply, we assume the command timed out (and connection
    is likely broken or frozen).

    Setting this variable to 0 disables timeout detection.

    By default - 2 minutes.
    """

    disable_nagle = True
    """
    Should Nagle algorithm (combining many small packets into bigger one)
    be disabled on the connection? Do not touch this setting unless you have reasons to.

    By default - True as in FICS client case reaction time is more important than throughput.
    Switching to False may make sense in programs which issue huge amount of commands.
    """

    min_interval_between_commands = None
    """
    If set, enforces given minimal time interval between sending two successive commands
    to FICS. The setting may be useful as traffic control measure in bots/clients
    which execute a lot of successive commands.

    Example:

        self.min_interval_between_commands = 0.01

    By default not set, commands are sent as fast as possible.
    """

    public_list_cache_age = 24 * 60 * 60
    """
    For how long is information about TD list, computer list and similar global
    list cached (for use in method like list_tds, list_computers, is_TD, am_I_TD, is_computer).
    By default 24 hours, as those change rarely.
    """

    private_list_cache_age = 5 * 60
    """
    For how long is information about subscribed channels, set notify variables etc
    cached. By default 5 minutes
    """
    # TODO: introduce cache reset on modifying commands.

    ################################################################################
    # Connection info and some frequently calculated data
    ################################################################################

    @property
    def fics_user_name(self):
        """
        Returns FICS user name of the current connection. Note that
        it need not be identical to nick specified on login (in case
        of guest login here we obtain sth like 'Guest73434', in case
        of true accounts upper/lowercase may differ).

        :return: my username (in canonical form), None if we are not
            currently connected
        """
        return self.protocol.fics_handle

    def am_I_guest(self):
        """
        Returns true if we are connected as guest, false if as true user..
        """
        return self.protocol.is_guest()

    def am_I_TD(self):
        """
        Retursns true if we have TD permission. Note that result
        is returned as deferred (checking the permission sometimes requires
        executing FICS command), so must be used as
             self.am_I_TD().addCallback(useResult)
        The information is cached and reused, to refresh it one must disconnect
        """
        return self.protocol.is_TD()

    ################################################################################
    # Event handling
    ################################################################################

    def on_fics_information(self, what, args):
        """
        Called whenever some FICS event is received (like info about tell, game more,
        user connection etc etc). Does nothing by default, can be overridden.

        :param what: Event name. Constant like 'channel_tell', 'game_move' etc.
            See mekk.fics.parsing.info_parser.parse_fics_line for all possible values.
        :param args: Dictionary of associated parameters. Fields and types
            depend on what (for example, when what='tell', args is AttributedTell
            object. See mekk.fics.parsing.info_parser.parse_fics_line for details.
        :returns deferred fired once the processing of event is completely finished
            (returning this deferred is not absolutely necessary, but recommended,
            as it makes it possible to keep track of processing and to sync unit-test code)
        """
        return defer.succeed(None)

    def on_login(self, user):
        """
        Called after succesfull login (or re-login in case of disconnection).
        This is the place to initiate actual bot/client processing.
        :param user: Name we use on FICS (actual guest name in case of guest login).
        """
        return defer.succeed(None)

    def on_logout(self):
        """
        Called after connection to FICS is closed. May cleanup.

        Note: in some cases (really fast connection drop) on_logout may be called
        without on_login or after only part on_login executed.
        """
        return defer.succeed(None)

    def on_command_timeout(self):
        """
        Called whenever we detect that some issued command timed out.
        Such case means frozen, dropped, or at least heavily lagging connection.

        Default implementation stops reactor (finishes the program).
        """
        logger.info("Stopping program due to command timeout (suspected connection freeze).")
        reactor.stop()
        # TODO: maybe it makes more sense to lose the connection than to stop the reactor?

    def on_fics_unknown(self, what):
        """
        Called instead of on_fics_information when some text
        can not be recognized and classified. In normal cases
        this method should not neet to be used, but provides possibility
        for temporary workarounds if some sensible information
        is not handled by mekk.fics (still, please report such
        cases...).

        By default does nothing (the case is logged as warning earlier)

        :param what: unknown text
        :type what: str
        """
        #logger.info("Unknown FICS notification: '%s'" % what)
        return defer.succeed(None)

    ################################################################################
    # FICS command execution - low level
    ################################################################################

    def run_command(self, command):
        """Execute given command, specified as text, Returns deferred
        firec once reply is obtained, with the reply text as content.

        :param command Command text (for example "tell John Welcome to the tournament")
        :return deferred fired with command result (or empty text if there is no result)
        """
        if len(command) > MAX_COMMAND_SIZE:
            if not command.startswith("qtell "):
                raise errors.FicsCommandTooLong(command)
        return self.protocol.run_command(command)

    def run_command_ext(self, command):
        """
        Slightly more elaborate alternative to run_command. The only difference
        is the result, instead of just „reply” to the command, returned is a pair
        (command name, command reply).

        In most cases it is not needed but is handy if some detailed logging is needed,
        or detection of unparsed replies (for which command name is "unknown").

        :param command: Command text (for example "observe 24")
        :type command: str
        :return: deferred fired with pair (command name, command result)
        :rtype: defer.Deferred(str, namedtuple)
        """
        return self.protocol.run_command_ext(command)

    ################################################################################
    # FICS command execution - high level / settings
    ################################################################################

    def set_finger_line(self, line_no, line_text):
        """
        Sets given finger line to given text. For example

        self.set_finger_line(1, "This is great bot.")

        :param line_no: line number (1-10)
        :param line_text: line (trailine newline will be stripped if present, line, can be empty)
        :return: deferred fired once the command is finished
        """
        line_text = line_text.rstrip(" \r\n")
        if line_text == "":
            line_text = "."
        return self.run_command("set %d %s" % (line_no, line_text))

    def set_finger(self, finger_text):
        """
        Sets whole finger in one run. The param is split into lines,
        successive lines are set at appropriate positions.

        :param finger_text (multiline) finger text. Must not contain more than
        10 lines (not counting final empty lines). Can be shorter (mussing lines
        are set to be empty)
        """
        fingerList = finger_text.split("\n")
        while fingerList:
            if fingerList[-1] == "":
                fingerList.pop(-1)
            else:
                break
        finLen = len(fingerList)
        if finLen > 10:
            raise errors.FingerTooLong
        while finLen < 10:
            fingerList.append("")
            finLen += 1
        rd = [ self.set_finger_line(i+1, fingerList[i])
               for i in range(finLen) ]
        return defer.DeferredList(rd)

    def subscribe_channel(self, no):
        """
        Start subscribing channel of given number
        :param no: channel number
        """
        return self.run_command("+channel %d" % no)
    def unsubscribe_channel(self, no):
        """
        Stop subscribing channel of given number
        :param no: channel number
        """
        return self.run_command("-channel %d" % no)
    def enable_shouts(self):
        """
        Start listening to shouts and cshouts
        """
        return defer.DeferredList( [
            self.run_command("set shout 1"),
            self.run_command("set cshout 1"),
            ])
    def disable_shouts(self):
        """Stop listening to shouts and cshouts"""
        return defer.DeferredList( [
            self.run_command("set shout 0"),
            self.run_command("set cshout 0"),
            ])
    def enable_seeks(self):
        """Start following seeks"""
        return self.run_command('set seek 1')
        # self.sendLine(iset seekinfo 1), self.sendLine(iset seekremove 1)
    def disable_seeks(self):
        """Stop following seeks"""
        return self.run_command('set seek 0')
    def enable_guest_tells(self):
        """Start listening to guests"""
        return self.run_command('set tell 1')
    def disable_guest_tells(self):
        """Stop listening to guests"""
        return self.run_command('set tell 0')
    def enable_games_tracking(self):
        """Start listening to notifications about games started and finished on the server"""
        return defer.DeferredList( [
            self.run_command("set gin 1"), # Normal notification
            self.run_command("iset allresults 1"), # Include adjudication results ('{Game 47 (MAd vs. pgv) MAd wins by adjudication} 1-0')
        ])
    def disable_games_tracking(self):
        """Stop listening to game start/stop notifications"""
        return self.run_command("set gin 0")
    def enable_users_tracking(self):
        """Start listening to player login/logout notifications"""
        return self.run_command("set pin 1") # player logins and logouts
    def disable_users_tracking(self):
        """Stop listening to login/logout notifications"""
        return self.run_command("set pin 0")

    ################################################################################
    # FICS command execution - high level utility functions
    ################################################################################

    def start_observing_game(self, gameno):
        """
        Start observing game of given number. If the command worked (it can fail
        if the game is private or if there is no such game), the result
        contains the detailed information about the game. If it failed, errs back
        with proper exception.

        :param gameno Game to observe (can be int or str)
        :returns (in case of success) deferred fired with ObservedGame
        """
        return self.run_command("observe %s" % gameno)

    def stop_observing_game(self, gameno):
        """
        Stop following given game
        :param gameno Game to observe (can be int or str)
        """
        return self.run_command("unobserve %s" % gameno)

    def get_game_info(self, gameno):
        """
        Get game info (via ginfo) command
        :param gameno Game to observe (can be int or str)
        """
        return self.run_command("ginfo %s" % gameno)

    def tell_to(self, who, what):
        """
        Tell to player who text what - using usual tell.

        Note: beware a danger of tell loops, when two bots
        tells one another an error in tight loop (bot A
        says to bot B something, for one reason or another,
        bot B answers "bad command" or similarly, bot A
        replies "invalid command", bot B replies "bad command"
        and so on… To mitigate this risk,
        mekk.fics.support.tell_status.TellLoopPrevention can
        be used.

        :param who: tell recipient (player name)
        :type who: PlayerName or str
        :param what: a message, can be a:
            - list of strings (will be sent on after another, strings
                should not contain newline)
            - text without newline (will be sent as single tell)
            - text contaning newlines (will be split on newlines and sent as series of tells)
        :return: deferred fired once tell is completely sent
        :rtype: defer.Deferred
        """
        if not type(what) == list:
            what = what.split("\n")
        return self._tell_to(who, what)

    @defer.inlineCallbacks
    def qtell_to(self, who, what):
        """
        Tell to player who the text what. If we have TD permission, use
        qtell (non-prefixed tell version suitable for longer information,
        similar to what mamer or relay use). If we do not have TD, use
        normal tell (= work as tell_to).

        Note: if text is long, can be split into a few qtells.

        :param: who: tell recipient (player name)
        :param: what: a message, can be a:
            - list of strings (will be sent on after another, strings
                should not contain newline)
            - text without newline (will be sent as single tell)
            - text contaning newlines (will be split on newlines and sent as series of tells)
        """
        if not type(what) == list:
            what = what.split("\n")
        can_use_qtell = yield self.am_I_TD()
        if can_use_qtell:
            yield self._qtell_to(who, what)
        else:
            yield self._tell_to(who, what)
    def _tell_to(self, who, what):
        """
        Actual implementation of tell sequence
        """
        if what:
            return defer.DeferredList([
                self.run_command("tell %s %s" % (who, item))
                for item in what])
        else:
            return defer.succeed(None)
    def _qtell_to(self, who, what):
        """
        Actual qtell execution. Splits text in parts if it is long, to avoid
        exceeding FICS limits.
        """
        cbs = []
        while what:
            body = "\\n".join(what[:10])
            if body.strip(): # Zabezpieczenie przed pustymi wierszami
                cbs.append( self.run_command("qtell %s %s" % (who, body)) )
            if len(what) > 10:
                what = what[10:]
            else:
                what = []
        return defer.DeferredList(cbs)

    @defer.inlineCallbacks
    def matching_players(self, name):
        """
        Finds all player names matching given name and returns them
        in a canonical form. This method can be used to:

        - find full-length player name when only truncated one is known,
        - verify player existence,
        - search for players

        Note: it returns all names which match given prefix, even if
        there exist exact match (matching_players('Mek') return 'Mek',
        'Mekk', ... and more). See canonical_name for another behaviour.

        :param name: player name or its prefix
        :type name: str or PlayerName
        :return: list of matching names
        :rtype: [PlayerName]
        """
        reply = yield self.run_command("handle %s" % name)
        defer.returnValue(reply.items)

    @defer.inlineCallbacks
    def canonical_name(self, truncated_name):
        """
        Converts player name (possibly truncated or with non-typical upper/lower case)
        to the canonical form. Works identically as matching_players, but guarantees
        to return single scalar reply (fails if name is non-unique or not found)

        :param truncated_name: player name or its prefix
        :type truncated_name: str or PlayerName
        :return: canonical name
        :rtype: PlayerName
        """
        items = yield self.matching_players(truncated_name)
        if len(items) == 1:
            defer.returnValue(items[0])
        elif items:
            if items[0] == truncated_name:
                defer.returnValue(items[0])
            else:
                raise errors.AmbiguousPlayer(truncated_name, items)
        else:
            raise errors.UnknownPlayer(truncated_name)

    ##########################################################################
    # List access
    ##########################################################################

    def list_subscribed_channels(self):
        """
        Finds all channels we are currently subscribed to
        :return: channel list
        :rtype: [ str ]
        """
        return self.protocol.fics_list_items(
            "channel", self.private_list_cache_age, treat_as_ints=True)

    def list_computers(self):
        """
        Returns list of all computer players (=computers)
        :return: list of computer names
        :rtype: [ str]
        """
        return self.protocol.fics_list_items(
            "computer", self.public_list_cache_age, treat_as_players=True)
    def list_tds(self):
        """
        Returns list of all TD accounts (=TD)
        :return: list of computer names
        :rtype: [ str]
        """
        return self.protocol.fics_list_items(
            "td", self.public_list_cache_age, treat_as_players=True)

    @defer.inlineCallbacks
    def is_computer(self, player):
        """
        Checks whether given player is a computer (is on computer list)
        :param player: player name
        :type player: str
        :return: is player on computer(s) list
        :rtype: bool
        """
        computers = yield self.list_computers()
        # TODO: handle PlayerName type and handle loose comparison
        defer.returnValue(player in computers)

    @defer.inlineCallbacks
    def is_TD(self, player):
        """
        Is given player TD (= bot with power)
        :param player: player name
        :type player: str
        :return: true if player is TD, else otherwise
        :rtype: bool
        """
        tds = yield self.list_tds()
        # TODO: handle PlayerName and handle loose comparison
        defer.returnValue(player in tds)


# TODO: maybe commands also should be implemented as mixin (or a few mixins?)


