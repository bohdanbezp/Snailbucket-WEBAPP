# -*- coding: utf-8 -*-

"""
Support for onTell, onGameStarted etc etc
"""

# pylint: disable=no-self-use

from twisted.internet import defer
from mekk.fics import datatypes
from mekk.fics.fics_client import logger

class FicsEventMethodsMixin(object):
    """
    The class which augments FicsClient object (or similar object) so
    instead of handling all events via single on_fics_information
    method, user code provides specific events handlers via methods
    like on_tell or on_game_move.  All events have default, empty
    implementation, handle just those which are needed.

    To use, inherit from both this class, and FicsClient:

    class MyClient(FicsEventMethodsMixin, FicsClient):
        # ...

    Note: there is exact correspondence between this object and
    info_parser.parse_fics_line, method names here are created by
    prefixing `on_` before the event label, and the event parameter
    object is parameter of such a method.

    In some delegation scenarios this mixin may be used to augment
    objects other than FicsClient.  The only expectation is that
    augmented object provides self.label (used in logging) and needs
    implementation of self.on_fics_information.
    """

    def on_fics_information(self, what, args):
        """
        Override of default function. Delegates to other methods.
        :param what: event name
        :param args: structured params
        """
        if what == 'game_started_ext':
            what = 'game_started'

        method_name = 'on_%s' % what
        method = getattr(self, method_name, None)
        if method:
            #noinspection PyCallingNonCallable
            return method(args)
        else:
            logger.warn("%s: No method %s", self.label, method_name)
            return defer.succeed("UNHANDLED")

    def on_tell(self, tell_info):
        """
        Called whenever we receive some direct tell.

        :param tell_info: info who said what (info.player, info.text)
        :type tell_info: AttributedTell
        :return: deferred fired once processing is finished (or
                 err-ed when it fails)
        """
        assert isinstance(tell_info, datatypes.AttributedTell)
        return defer.succeed(None)

    def on_qtell(self, qtell_text):
        """
        Called whenever we receive some qtell (:started line used by
        advanced bots).  Each line is reported separately (we get one
        on_qtell for every line).

        :param qtell_text: text obtained (without newline)
        :type qtell_text: str

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(qtell_text, str)
        return defer.succeed(None)

    def on_announcement(self, announcement):
        """
        Called whenever we see FICS announcement by some admin.

        :param announcement: announcement details (player and text)
        :type announcement: AttributedTell
        :return: deferred fired once processing is finished (or err-ed
            when it fails)
        """
        assert isinstance(announcement, datatypes.AttributedTell)
        return defer.succeed(None)

    def on_user_connected(self, player):
        """
        Called whenever some user connects to FICS.  To see those
        events for all players, one must either call
        enableUsersTracking() (set gin), otherwise only players on
        notify list are reported.

        :param player: player who just connected
        :type player: PlayerName

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(player, datatypes.PlayerName)
        return defer.succeed(None)
        # TODO: test notifications for users on notify list

    def on_user_disconnected(self, player):
        """
        Called whenever some user disconnects from FICS.  To see those
        events for all players, one must either call
        enableUsersTracking() (set pin), otherwise only players on
        notify list are reported.

        :param player: player who just connected
        :type player: PlayerName

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(player, datatypes.PlayerName)
        return defer.succeed(None)

    def on_game_started(self, game):
        """
        Called whenever some game starts.  All games are reported if
        enableGamesTracking() was called (set gin), otherwise only
        players from gnotify list are handled this way.

        :param game: started game details
        :type game: GameStart or GameStartExt (the latter if notification
             came from gnotify)

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(game, datatypes.GameStart) or isinstance(game, datatypes.GameStartExt)
        return defer.succeed(None)
        # TODO test gnotify games handling

    def on_game_joined(self, game):
        """
        Called whenever we start playing or observing a game.
        Provides rich info about one.

        :param game: joined game details
        :type game: GameJoinInfo

        :return: deferred fired once processing is finished
        """
        assert isinstance(game, datatypes.GameJoinInfo)

    def on_game_finished(self, game):
        """
        Called whenever some game finishes.  All games are reported if
        enableGamesTracking() was called (set gin), otherwise only
        observed games and games of players from gnotify list are
        reported

        :param game: finished game details
        :type game: GameFinish

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(game, datatypes.GameFinish)
        return defer.succeed(None)

    def on_game_move(self, game_move):
        """
        Called whenever new move is made in some played or observed
        game.

        :param game_move: Info about the move.
        :type game_move: GameMove

        :return: deferred fired once processing is finished (or err-ed
            when it fails)
        """
        assert isinstance(game_move, datatypes.GameMove)
        return defer.succeed(None)

    def on_game_kibitz(self, game_kibitz):
        """
        Called whenever some kibitz or whisper is made in played or
        observed game.

        :param game_kibitz: info who said what (and in which game)
        :type game_kibitz: GameKibitz

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(game_kibitz, datatypes.GameKibitz)
        return defer.succeed(None)

    def on_game_note(self, note):
        """
        Called whenever ”something happens” in the game - something is
        offered (draw, pause, abort, adjourn, takeback …), offer is
        accepted, clock is updated etc etc.

        Note: some of those events may be handled separately in the
        future.

        :param note: info what happened
        :type note: GameNote

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(note, datatypes.GameNote)
        return defer.succeed(None)
        # TODO: extract some of those (at least accepted offers)

    def on_channel_tell(self, tell_info):
        """
        Called whenever we hear tell on one of the subscribed channels
        (note subscribe_channel method).

        :param tell_info: info who said what and where
        :type tell_info: ChannelTell
        :return: deferred fired once processing is finished (or err-ed when it fails)
        """
        assert isinstance(tell_info, datatypes.ChannelTell)
        return defer.succeed(None)

    def on_shout(self, shout_info):
        """
        Called whenever we hear some shout.  Note that one must use
        enable_shouts to hear them.

        :param shout_info: info who said what (info.player, info.text)
        :type shout_info: AttributedTell

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(shout_info, datatypes.AttributedTell)
        return defer.succeed(None)

    def on_it_shout(self, shout_info):
        """
        Called whenever we hear some "it-style" shout ("--> Mekk loves
        teamleague!").  Note that one must use enable_shouts to hear
        them.

        This method by default delegates processing to on_shout, so if
        there is no need to distinguish between shout types, need not
        be overridden.

        :param shout_info: info who said what (info.player, info.text)
        :type shout_info: AttributedTell

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(shout_info, datatypes.AttributedTell)
        return self.on_shout(shout_info)

    def on_cshout(self, shout_info):
        """
        Called whenever we receive some c-shout (shout about chess). Note that
        one must enable_shouts to hear them.

        :param shout_info: info who said what (info.player, info.text)
        :type shout_info: AttributedTell
        :return: deferred fired once processing is finished (or err-ed when it fails)
        """
        assert isinstance(shout_info, datatypes.AttributedTell)
        return defer.succeed(None)

    def on_seek(self, seek):
        """
        Called whenever we see new seek offered.  Note that one must
        enable_seeks to see them.

        :param seek: seek details
        :type seek: Seek

        :return: deferred fired once processing is finished (or err-ed
                 when it fails)
        """
        assert isinstance(seek, datatypes.Seek)
        return defer.succeed(None)

    def on_seek_removed(self, seeks):
        """
        Called whenever we see some seek(s) removed.

        :param seeks: list of removed seeks
        :type seeks: [SeekRef]
        :return: deferred fired once processing is finished (or err-ed when it fails)
        """
        assert isinstance(seeks, list)
        return defer.succeed(None)

    def on_observing_finished(self, game_reference):
        assert isinstance(game_reference, datatypes.GameReference)
        return defer.succeed(None)
