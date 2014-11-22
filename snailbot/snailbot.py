# -*- coding: utf-8 -*-

"""
snailbot
=====================
The bot to manage Snailbucket tourneys on Free Internet Chess Server
based on mekk.fics library.

"""
from __future__ import print_function

import re
import getopt, sys, logging, os
import MySQLdb
from twisted.internet import defer, reactor, task
from twisted.enterprise import adbapi
from twisted.python import log

from mekk.fics import ReconnectingFicsFactory, FicsClient, FicsEventMethodsMixin
from mekk.fics import TellCommandsMixin, TellCommand

logger = logging.getLogger("snail")

#################################################################################
# Configuration
#################################################################################

from mekk.fics import FICS_HOST, FICS_PORT

FICS_USER='snailbotguest'
FICS_PASSWORD=''

FINGER_TEXT = """Snailbot v.20141123

Join Snail Bucket http://snailbucket.org/ FICS chess community for some loooong time controls.

This bot is run by Bodia
Usage:
    tell snailbot join
    tell snailbot play
    tell snailbot help
"""

script_dir = os.path.dirname(os.path.abspath(__file__))


ROUND_CURR = "1"


class ReconnectingConnectionPool(adbapi.ConnectionPool):
    """Reconnecting adbapi connection pool for MySQL.

    This class improves on the solution posted at
    http://www.gelens.org/2008/09/12/reinitializing-twisted-connectionpool/
    by checking exceptions by error code and only disconnecting the current
    connection instead of all of them.

    Also see:
    http://twistedmatrix.com/pipermail/twisted-python/2009-July/020007.html

    """
    def _runInteraction(self, interaction, *args, **kw):
        try:
            return adbapi.ConnectionPool._runInteraction(self, interaction, *args, **kw)
        except MySQLdb.OperationalError, e:
            if e[0] not in (2006, 2013):
                raise
            print("RCP: got error %s, retrying operation" %(e))
            conn = self.connections.get(self.threadID())
            self.disconnect(conn)
            # try the interaction again
            return adbapi.ConnectionPool._runInteraction(self, interaction, *args, **kw)

#################################################################################
# ”Business logic” (processing not directly bound to FICS interface)
#################################################################################

class SnailBot(object):
    def __init__(self, dbpool):
        self.dbpool = dbpool

    def __del__(self):
        self.dbpool.close()

    db_initialized = False

    def save_unregistered_player(self, who):     
        return self.dbpool.runQuery("INSERT INTO MEMBERS(CONFIRMED, GRUP, USERNAME) VALUES (0, 1, '"+who+"')")

    # TODO: move logic of "play" command here

#################################################################################
# Commands (handling tells to the bot)
#################################################################################

class JoinCommand(TellCommand):
    """
    Join snailbucket community
    """

    def __init__(self, clock_statistician):
        self.clock_statistician = clock_statistician
    @classmethod
    def named_parameters(cls):
        return {}
    @classmethod
    def positional_parameters_count(cls):       
        return 0, 0
    @defer.inlineCallbacks
    def run(self, fics_client, player, *args, **kwargs):
        yield self.clock_statistician.save_unregistered_player(player.name)
        yield fics_client.tell_to(player, "You are registered. Please use the following form to proceed: http://www.snailbucket.org/wiki/Special:Register")

    def help(self, fics_client):
        return "Initiates the join to SnailBucket. See more at http://snailbucket.org/wiki/FAQ"


class PlayCommand(TellCommand):
    """
    Play scheduled snailbucket game
    """

    def __init__(self, clock_statistician):
        self.clock_statistician = clock_statistician

    ##
    # http://snailbucket.org/wiki/Matching_time_controls_algorithm
    ##
    def recommend_time(self, white_preference, black_preference):
        stripped_white = [x.strip().replace("45 45", "45_45") for x in white_preference.split(",")]
        stripped_black = [x.strip().replace("45 45", "45_45") for x in black_preference.split(",")]

        def intersect(a, b):
            return list(set(a) & set(b))

        inters = intersect(stripped_white, stripped_black)

        best_value = 1000
        best_tc = "45_45"
        for tc in inters:
            of = stripped_white.index(tc) + stripped_black.index(tc)
            if of < best_value:
                best_value = of
                best_tc = tc
            elif of == best_value:
                if int(tc.replace("75_0", "75_00").replace("_", "")) < int(best_tc.replace("75_0", "75_00").replace("_", "")):
                    best_value = of
                    best_tc = tc

        return best_tc

    ##
    # Return the game parameters of caller's scheduled game if there is any
    ##    
    def get_game_data(self, caller):

        def stat(tx):
            r = tx.execute("select TOURN_PLAYERS.ID from TOURN_PLAYERS inner join MEMBERS on MEMBERS.ID = TOURN_PLAYERS.MEMBER_ID"
                           " where MEMBERS.username = '"+caller+"'")
            player_id = str(tx.fetchall()[0][0])
            tx.execute(
            "select ID,ROUND from TOURN_GAMES where (BLACKPL_ID = "+player_id+" or WHITEPL_ID = "+player_id+") and SHEDULED_DATE IS NOT NULL "
                                                                                                      "and RESULT IS NULL ORDER BY SHEDULED_DATE ASC"
            )
            vals = tx.fetchall()[0]
            game_id = str(vals[0])
            round = str(vals[1])
            tx.execute("select MEMBERS.USERNAME, MEMBERS.PREFERENCE from MEMBERS inner join TOURN_PLAYERS on MEMBERS.ID = TOURN_PLAYERS.MEMBER_ID where "
                    "TOURN_PLAYERS.ID = (select WHITEPL_ID from TOURN_GAMES where ID="+game_id+")")
            white_username, white_preference = tx.fetchall()[0]
            tx.execute("select MEMBERS.USERNAME, MEMBERS.PREFERENCE from MEMBERS inner join TOURN_PLAYERS on MEMBERS.ID = TOURN_PLAYERS.MEMBER_ID where "
                   "TOURN_PLAYERS.ID = (select BLACKPL_ID from TOURN_GAMES where ID="+game_id+")")
            black_username, black_preference = tx.fetchall()[0]
            return (white_username, black_username, self.recommend_time(white_preference, black_preference).replace("_", " "), round)

        return dbpool.runInteraction(stat)


    @classmethod
    def named_parameters(cls):
        return {}
    @classmethod
    def positional_parameters_count(cls):
        return 0, 0
    @defer.inlineCallbacks
    def run(self, fics_client, player, *args, **kwargs):

        game_start_issued = False

        @defer.inlineCallbacks
        def process(res):
            global ROUND_CURR
            ROUND_CURR = res[3]

            if res[0] == player.name:
                # finger = yield fics_client.run_command("log %s" % (res[1]))
                #
                # if "On for:" not in finger or "Idle:" not in finger:
                #     fics_client.tell_to(player, res[1] + " is not logged in")
                # else:
                    vars = yield fics_client.run_command("var %s" % (res[0]))
                    vars1 = yield fics_client.run_command("var %s" % (res[1]))
                    if "noescape=1" in str(vars):
                        fics_client.tell_to(player, "Please execute command \"set noescape 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "rated=0" in str(vars):
                        fics_client.tell_to(player, "Please execute command \"set rated 1\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "kibitz=1" in str(vars):
                        fics_client.tell_to(player, "Please execute command \"set kibitz 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "notakeback=0" in str(vars):
                        fics_client.tell_to(player, "Please execute command \"set notakeback 1\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "private=1" in str(vars):
                        fics_client.tell_to(player, "Please execute command \"set private 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "noescape=1" in str(vars1):
                        fics_client.tell_to(player, "Your opponent should execute command \"set noescape 0\" before playing the game.")
                        fics_client.tell_to(res[1], "Please execute command \"set noescape 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "rated=0" in str(vars1):
                        fics_client.tell_to(player, "Your opponent should execute command \"set rated 1\" before playing the game.")
                        fics_client.tell_to(res[1], "Please execute command \"set rated 1\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "notakeback=0" in str(vars1):
                        fics_client.tell_to(player, "Your opponent should execute command \"set notakeback 1\" before playing the game.")
                        fics_client.tell_to(res[1], "Please execute command \"set notakeback 1\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "kibitz=1" in str(vars1):
                        fics_client.tell_to(player, "Your opponent should execute command \"set kibitz 0\" before playing the game.")
                        fics_client.tell_to(res[1], "Please execute command \"set kibitz 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if " private=1" in str(vars1):
                        fics_client.tell_to(player, "Your opponent should execute command \"set private 0\" before playing the game.")
                        fics_client.tell_to(res[1], "Please execute command \"set private 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "noescape=1" not in str(vars) and "noescape=1" not in str(vars1) and "rated=0" not in str(vars) and "rated=0" not in str(vars1) \
                            and "kibitz=1" not in str(vars) and "kibitz=1" not in str(vars1) and "notakeback=0" not in str(vars) and "notakeback=0" not in str(vars1) \
                            and " private=1" not in str(vars) and " private=1" not in str(vars1):
                        yield fics_client.run_command("+gnotify %s" % (player.name))
                        yield fics_client.tell_to(player, "==== Snailbucket game start issued ====")
                        yield fics_client.tell_to(res[1], "==== Snailbucket game start issued ====")
                        fics_client.run_command("rmatch %s %s %s %s" % (res[0], res[1], res[2], "white"))
            else:
                # finger = yield fics_client.run_command("log %s" % (res[0]))

                # if "On for:" not in finger or "Idle:" not in finger:
                #     fics_client.tell_to(player, res[0] + " is not logged in")
                # else:
                    vars = yield fics_client.run_command("var %s" % (res[0]))
                    vars1 = yield fics_client.run_command("var %s" % (res[1]))
                    if "noescape=1" in str(vars1):
                        fics_client.tell_to(player, "Please execute command \"set noescape 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "rated=0" in str(vars1):
                        fics_client.tell_to(player, "Please execute command \"set rated 1\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "kibitz=1" in str(vars1):
                        fics_client.tell_to(player, "Please execute command \"set kibitz 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "notakeback=0" in str(vars1):
                        fics_client.tell_to(player, "Please execute command \"set notakeback 1\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "private=1" in str(vars1):
                        fics_client.tell_to(player, "Please execute command \"set private 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "noescape=1" in str(vars):
                        fics_client.tell_to(player, "Your opponent should execute command \"set noescape 0\" before playing the game.")
                        fics_client.tell_to(res[0], "Please execute command \"set noescape 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "rated=0" in str(vars):
                        fics_client.tell_to(player, "Your opponent should execute command \"set rated 1\" before playing the game.")
                        fics_client.tell_to(res[0], "Please execute command \"set rated 1\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "notakeback=0" in str(vars):
                        fics_client.tell_to(player, "Your opponent should execute command \"set notakeback 1\" before playing the game.")
                        fics_client.tell_to(res[0], "Please execute command \"set notakeback 1\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "kibitz=1" in str(vars):
                        fics_client.tell_to(player, "Your opponent should execute command \"set kibitz 0\" before playing the game.")
                        fics_client.tell_to(res[0], "Please execute command \"set kibitz 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if " private=1" in str(vars):
                        fics_client.tell_to(player, "Your opponent should execute command \"set private 0\" before playing the game.")
                        fics_client.tell_to(res[0], "Please execute command \"set private 0\" before playing a SnailBucket game and \"t snailbot play\" again.")

                    if "noescape=1" not in str(vars) and "noescape=1" not in str(vars1) and "rated=0" not in str(vars) and "rated=0" not in str(vars1)\
                            and "kibitz=1" not in str(vars) and "kibitz=1" not in str(vars1) and "notakeback=0" not in str(vars) and "notakeback=0" not in str(vars1)\
                            and " private=1" not in str(vars) and " private=1" not in str(vars1):
                        yield fics_client.run_command("+gnotify %s" % (player.name))
                        yield fics_client.tell_to(player, "==== Snailbucket game start issued ====")
                        yield fics_client.tell_to(res[0], "==== Snailbucket game start issued ====")
                        fics_client.run_command("rmatch %s %s %s %s" % (res[1], res[0], res[2], "black"))

        x = self.get_game_data(player.name)
        x.addCallback(process)
        yield x


    def help(self, fics_client):
        return "Start a snailbucket game"



class HelpCommand(TellCommand):
    """
    Help command: TODO: make it work
    """
  
    @classmethod
    def named_parameters(cls):
        return {}
    @classmethod
    def positional_parameters_count(cls):
        return 0,1

    def run(self, fics_client, player, *args, **kwargs):
        if args[0]:
            return fics_client.command_help(args[0][0])
        else:
            return "I support the following commands: %s.\nFor more help try: %s" % (
                ", ".join(fics_client.command_names()),
                ", ".join(
                    "\"tell %s help %s\"" % (fics_client.fics_user_name, command)
                    for command in fics_client.command_names()
                    if command != "help"))

    def help(self, fics_client):
        return "I print some help"

#################################################################################
# The bot core
#################################################################################

class MyBot(
    TellCommandsMixin,
    FicsEventMethodsMixin,
    FicsClient
):

    def __init__(self, clock_statistician):
        FicsClient.__init__(self, label="clock-stats-bot")

        self.clock_statistician = clock_statistician

        self.use_keep_alive = True
        self.variables_to_set_after_login = {
            'shout': 0,
            'cshout': 0,
            'tzone': 'EURCST',
            'tell': 0,
            'noescape': 0,
            'kibitz': 0,
            # Enable guest tells
            'guest': 0,
            # Listen to games notifications
            'gin' : 0,
            }
        self.interface_variables_to_set_after_login = [
            # For rich info about game started
            ]

        self.register_command(JoinCommand(self.clock_statistician))
        self.register_command(PlayCommand(self.clock_statistician))
        self.register_command(HelpCommand())

        self.ongoing_games = []

    def _notify_finger(self):
        for game in self.ongoing_games:
            self.run_command("t 101 Snailbucket game in progress: " + game["white"] + "(" +game["white_rank"]+ ")" + " vs. " + game["black"] + "(" +game["black_rank"]+ ")" + " -- Round "+ ROUND_CURR +": \"observe " + game["game_no"] + "\" to watch")


    def on_login(self, my_username):
        print("I am logged as %s, use \"tell %s help\" to start conversation on FICS" % (
            my_username, my_username))

        self._gamenotify_task = task.LoopingCall(self._notify_finger)
        self._gamenotify_task.start(900, now=True)

        # Normal post-login processing
        return defer.DeferredList([
                self.set_finger(FINGER_TEXT),
                # Commands below are unnecessary as variables_to_set_after_login above
                # defines them. Still, this form may be useful if we dynamically enable/disable
                # things.
                #  self.enable_seeks(),
                #  self.enable_guest_tells(),
                #  self.enable_games_tracking(),
                #  self.enable_users_tracking(),
                self.subscribe_channel(101),
                self.unsubscribe_channel(49), # TODO: tournament stats
                self.unsubscribe_channel(50),
                self.unsubscribe_channel(2),
                # self.subscribe_channel(90),
                self.run_command("+censor relay")
                ])

    def on_fics_unknown(self, what):
        m = re.search("Game notification:\s(?P<white>\w+)\s\(\s*(?P<white_rank>\d+|\-+|\++)\)\svs.\s(?P<black>\w+)\s\(\s*(?P<black_rank>\d+|\-+|\++)\)\s(?P<is_rated>rated|unrated)\s(?P<variant>[^\s]+)\s(?P<clock_base>\d+)\s(?P<clock_inc>\d+):\sGame\s(?P<game_no>\d+)",
                      what)
        global ROUND_CURR
        if m:
            self.run_command("t 101 Snailbucket game has started: " + m.group("white") + "(" +m.group("white_rank")+ ")" + " vs. " + m.group("black") + "(" +m.group("black_rank")+ ")" + " -- Round "+ ROUND_CURR +": \"observe " + m.group("game_no") + "\" to watch")
            self.start_observing_game(m.group("game_no"))

            curr_game = dict()
            curr_game['white'] = m.group("white")
            curr_game['white_rank'] = m.group("white_rank")
            curr_game['black'] = m.group("black")
            curr_game['black_rank'] = m.group("black_rank")
            curr_game['game_no'] = m.group("game_no")
            self.ongoing_games.append(curr_game)


    def on_game_finished(self, game):
        global ROUND_CURR
        self.run_command("t 101 Snailbucket game has ended: " + game.white_name.name + " vs. " + game.black_name.name + " -- Round "+ ROUND_CURR +": " + game.result + " {" + game.result_desc) + "}"
        self.run_command("-gnotify %s" % (game.white_name.name))
        self.run_command("-gnotify %s" % (game.black_name.name))

        for gm in self.ongoing_games:
            if gm['white'] == game.white_name.name:
                our_game = gm

        self.ongoing_games.remove(our_game)

    def on_logout(self):
        if hasattr(self, '_gamenotify_task'):
            self._gamenotify_task.stop()
            del self._gamenotify_task


#################################################################################
# Script argument processing
#################################################################################

# TODO: --silent with no logging except errors

options, remainders = getopt.getopt(args = sys.argv[1:], shortopts=[], longopts=["debug"])

if "--debug" in [name for name,_ in options]:
    logging_level = logging.DEBUG
else:
    #logging_level = logging.WARN
    logging_level = logging.INFO

logging.basicConfig(level=logging_level)

#################################################################################
# Startup glue code
#################################################################################

# TODO: convert back to reconnecting

dbpool = ReconnectingConnectionPool("MySQLdb", user="bodia", passwd="pass", db="test_db", cp_reconnect=True
)

clock_statistician = SnailBot(dbpool)
my_bot = MyBot(clock_statistician)
reactor.connectTCP(
    FICS_HOST, FICS_PORT,
    ReconnectingFicsFactory(
        client=my_bot,
        auth_username=FICS_USER, auth_password=FICS_PASSWORD)
)
#noinspection PyUnresolvedReferences
reactor.run()


