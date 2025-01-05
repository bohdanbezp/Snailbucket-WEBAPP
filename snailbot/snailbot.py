# -*- coding: utf-8 -*-

"""
snailbot
=====================
The bot to manage Snailbucket tourneys on Free Internet Chess Server
based on mekk.fics library.

"""
from __future__ import print_function

import configparser
import urllib.request
import urllib.parse
import urllib
import re
import getopt, sys, logging, os
import mysql.connector
import time
import traceback
from twisted.internet import defer, reactor, task
from twisted.enterprise import adbapi
from twisted.python import log
from snailbot.state import state

from mekk.fics import ReconnectingFicsFactory, FicsClient, FicsEventMethodsMixin
from mekk.fics import TellCommandsMixin, TellCommand

logger = logging.getLogger("snail")

################################################################################
# Configuration
################################################################################

from mekk.fics import FICS_HOST, FICS_PORT

# Read credentials from the properties file
script_dir = os.path.dirname(os.path.abspath(__file__))
properties_file = os.path.join(script_dir, "snailbot.properties")

config = configparser.ConfigParser()
config.read(properties_file)

FICS_USER = config.get('DEFAULT', 'fics_user')
FICS_PASSWORD = config.get('DEFAULT', 'fics_password')
DB_USER = config.get('DEFAULT', 'db_user')
DB_PASSWORD = config.get('DEFAULT', 'db_password')
DB_HOST = config.get('DEFAULT', 'db_host')
DB_DATABASE = config.get('DEFAULT', 'db_database')

FINGER_TEXT = """Snailbot v.20241113

Join Snail Bucket http://snailbucket.org/ \
FICS chess community for some loooong time controls.

This bot is run by Bodia
More about supported commands:
    tell %s help

If snailbot is logged off, please message Bodia, or pchesso, or (preferrably) email us at tds@snailbucket.org \
- thanks!
""" % (FICS_USER)

script_dir = os.path.dirname(os.path.abspath(__file__))

################################################################################
# ”Business logic” (processing not directly bound to FICS interface)
################################################################################

class SnailBot(object):
    def __init__(self, dbpool):
        self.dbpool = dbpool
        self.processing = False

    def __del__(self):
        self.dbpool.close()

    db_initialized = False

    def save_unregistered_player(self, who):
        return self.dbpool.runQuery(
            "INSERT INTO MEMBERS(CONFIRMED, GRUP, USERNAME) VALUES (0, 1, %s)",
            who)

    ##
    # http://snailbucket.org/wiki/Matching_time_controls_algorithm
    ##
    def recommend_time(self, white_preference, black_preference):
        stripped_white = [x.strip().replace("45 45", "45_45") 
                          for x in white_preference.split(",")]
        stripped_black = [x.strip().replace("45 45", "45_45")
                          for x in black_preference.split(",")]

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
                if (int(tc.replace("75_0", "75_00").replace("_", "")) <
                    int(best_tc.replace("75_0", "75_00").replace("_", ""))):
                    best_value = of
                    best_tc = tc

        return best_tc

    ##
    # Return the game parameters of caller's first scheduled game if there is
    # any.
    ##
    def get_game_data(self, caller):

        def _get_game_data(tx):
            # TODO: Update to allow players' games from not the newest tourneys.
            tx.execute(
                "select TOURN_PLAYERS.ID "
                "from TOURN_PLAYERS "
                "inner join MEMBERS "
                "on MEMBERS.ID = TOURN_PLAYERS.MEMBER_ID "
                "where MEMBERS.username = %s "
                "and TOURN_PLAYERS.TOURNEY_ID = "
                "  (select max(ID) from TOURNAMENTS) ",
                (caller,))
            (player_id,) = tx.fetchone()

            # TODO: update to fetch all pending games of the player.
            tx.execute(
                "select ID, ROUND "
                "from TOURN_GAMES "
                "where (BLACKPL_ID = %s or WHITEPL_ID = %s) "
                "and SHEDULED_DATE IS NOT NULL "
                "and RESULT IS NULL "
                "and TOURNEY_ID = (select max(ID) from TOURNAMENTS) "
                "order by SHEDULED_DATE asc ",
                (player_id, player_id))
            (game_id, rnd) = tx.fetchone()

            tx.execute(
                "select MEMBERS.USERNAME, MEMBERS.PREFERENCE "
                "from MEMBERS "
                "inner join TOURN_PLAYERS "
                "on MEMBERS.ID = TOURN_PLAYERS.MEMBER_ID "
                "where TOURN_PLAYERS.ID = "
                "  (select WHITEPL_ID from TOURN_GAMES where ID=%s) ",
                (game_id,))
            (white_username, white_preference) = tx.fetchone()

            tx.execute(
                "select MEMBERS.USERNAME, MEMBERS.PREFERENCE "
                "from MEMBERS "
                "inner join TOURN_PLAYERS "
                "on MEMBERS.ID = TOURN_PLAYERS.MEMBER_ID "
                "where TOURN_PLAYERS.ID = "
                "  (select BLACKPL_ID from TOURN_GAMES where ID=%s) ",
                (game_id,))
            black_username, black_preference = tx.fetchone()

            print('get_game: %s %s %s' % (white_username, black_username, rnd))
            return (
                white_username,
                black_username,
                self.recommend_time(white_preference, black_preference)
                    .replace("_", " "),
                rnd)

        return dbpool.runInteraction(_get_game_data)


    def updateGameStatus(self, caller, date):
        def stat(tx):
            r = tx.execute("select TOURN_PLAYERS.ID from TOURN_PLAYERS inner"
                " join MEMBERS on MEMBERS.ID = TOURN_PLAYERS.MEMBER_ID"
                " where MEMBERS.username = %s and"
                " TOURN_PLAYERS.TOURNEY_ID = (select max(ID) from TOURNAMENTS)",
                (caller,))
            (player_id,) = tx.fetchone()
            tx.execute("select ID from TOURN_GAMES where"
                " (BLACKPL_ID = %s or WHITEPL_ID = %s)"
                " and SHEDULED_DATE IS NOT NULL and RESULT IS NULL"
                " and TOURNEY_ID = (select max(ID) from TOURNAMENTS)"
                " ORDER BY SHEDULED_DATE ASC", (player_id, player_id))
            (game_id, ) = tx.fetchone()
            tx.execute("UPDATE TOURN_GAMES SET SHEDULED_DATE=%s WHERE ID=%s",
                       (date, game_id))
            return game_id

        return dbpool.runInteraction(stat)

    # TODO: move logic of "play" command here

################################################################################
# Commands (handling tells to the bot)
################################################################################

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
        def process(res):
            fics_client.tell_to(player, "You are registered. "
                "Please use the following form to proceed: "
                "http://www.snailbucket.org/wiki/Special:Register")

            values = {
                'to': 'tds@snailbucket.org',
                'from': 'notify@snailbucket.org',
                'toname': 'TDs',
                'subject': str(player.name) + ' has registered',
                'text': str(player.name) + ' has registered using the bot.',
                'api_user': '',
                'api_key': ''}

            data = urllib.urlencode(values)
            req = urllib.Request(
                "https://sendgrid.com/api/mail.send.json", data)
            response = urllib.urlopen(req)
            the_page = response.read()

        def errorHandler(e):
            if "1062" in str(e.getErrorMessage()):
                fics_client.tell_to(player, "You already have account, "
                    "you don't need to talk to the snailbot account on FICS. "
                    "You need to log in to the website, go to the "
                    "Participants section, and sign up.")
            else:
                fics_client.tell_to(player, "Error during joining the club. "
                    "Please contact Bodia if the problem persists.")

        xx = self.clock_statistician.save_unregistered_player(player.name)
        xx.addCallback(process)
        xx.addErrback(errorHandler)
        yield xx

    def help(self, fics_client):
        return ("Initiates the join to SnailBucket. "
                "See more at https://snailbucket.org/wiki/FAQ")
    


class SetmessageCommand(TellCommand):
    @defer.inlineCallbacks
    def run(self, fics_client, player, *args, **kwargs):
        if str(player.name) not in [
                "Bodia", "pchesso"]:
            yield fics_client.tell_to(player, "You're not the admin, sorry!")
            return
        state.state['channel_message'] = ' '.join(args[0])
        state.SaveState()
        yield fics_client.tell_to(player, "Ok.")

    def help(self, fics_client):
        return "Sets channel101 message."


    
class ExecuteCommand(TellCommand):
    """
    Exectutes admin's command on behalf of the bot account
    """

    def __init__(self, clock_statistician):
        self.clock_statistician = clock_statistician
    @defer.inlineCallbacks
    def run(self, fics_client, player, *args, **kwargs):
        if str(player.name) in [
                "Bodia", "pchesso"]:
            command_to_exec = ' '.join(args[0])
            resu = yield fics_client.run_command(command_to_exec)
            print("Command result: %s" % resu)
            if (resu is not None):
                fics_client.tell_to(player, "Success")
        else:
            fics_client.tell_to(player, "You're not the admin, sorry!")

    def help(self, fics_client):
        return "Exectutes admin's command on behalf of the bot account."


class PlayCommand(TellCommand):
    """
    Play scheduled snailbucket game
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

        @defer.inlineCallbacks
        def process(res):
            player_index = 0 if player.name == res[0] else 1
            player_name = res[player_index]
            opponent = res[1 - player_index]
            time_control = res[2]
            required_vars = [('noescape', 0), ('kibitz', 0),
                             ('notakeback', 1), ('private', 0), ('autoflag', 1)]
            player_vars = yield fics_client.run_command("var %s" % (player))
            opponent_vars = yield fics_client.run_command("var %s" % (opponent))
            print("player_vars: %s", player_vars)
            print("opponent_vars: %s", opponent_vars)

            vars_ok = True
            for (var, value) in required_vars:
                if ("%s=%d" % (var, 1-value)) in str(player_vars):
                    vars_ok = False
                    message = ('Please execute command "set {0} {1}" before playing ' +
                        'a SnailBucket game and "t snailbot play" again.').format(var, value)
                    print(message)
                    fics_client.tell_to(player, message)
                if ("%s=%d" % (var, 1-value)) in str(opponent_vars):
                    vars_ok = False
                    message = ('Your opponent should execute command "set {0} {1}" ' +
                        'before playing the game.').format(var, value)
                    print(message)
                    fics_client.tell_to(player, message)
                    fics_client.tell_to(opponent, 
                        'Please execute command "set {0} {1}" before playing ' +
                        'a SnailBucket game and "t snailbot play" again.'.format(var, value))

            print('vars_ok: %s' % str(vars_ok))
            if vars_ok:
                try:
                    yield fics_client.run_command("+gnotify %s" % (player.name))
                except Exception as e:
                    print(f"Failed to run +gnotify for player {player.name}: {e}")

                color = "white" if player_index == 0 else "black"
                command = "rmatch %s %s %s %s" % (player_name, opponent, time_control, color)
                print('running command: %s' % command)
                rmatch_res = yield fics_client.run_command(command)

        if not self.clock_statistician.processing:
            self.clock_statistician.processing = True
            try:
                x = self.clock_statistician.get_game_data(player.name)
                x.addCallback(process)
                yield x
            except Exception as e:
                fics_client.tell_to(player, "Error starting the game. "
                                    "Please contact Bodia if the problem persists.")
                # Print full traceback to the console
                print("PlayCommand of user %s failed with: %s" % (player, e))
                print(traceback.format_exc())  # Outputs the full traceback
            finally:
                self.clock_statistician.processing = False

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
            return ("I support the following commands: %s.\n"
                "For more help try: %s" % (
                ", ".join(fics_client.command_names()),
                ", ".join('"tell %s help %s"' % (fics_client.fics_user_name,
                                                command)
                    for command in fics_client.command_names()
                    if command not in ["help", "execute"])))

    def help(self, fics_client):
        return "I print some help"

################################################################################
# The bot core
################################################################################

class MyBot(
    TellCommandsMixin,
    FicsEventMethodsMixin,
    FicsClient):

    def __init__(self, clock_statistician):
        FicsClient.__init__(self, label="clock-stats-bot")

        self.clock_statistician = clock_statistician

        self.use_keep_alive = True
        self.variables_to_set_after_login = {
            'shout': 1,
            'cshout': 1,
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

        #self.register_command(JoinCommand(self.clock_statistician))
        self.register_command(PlayCommand(self.clock_statistician))
        self.register_command(ExecuteCommand(self.clock_statistician))
        self.register_command(SetmessageCommand())
        self.register_command(HelpCommand())

        self.on_game_started = self.on_fics_unknown

        self.ongoing_games = []        

    def _notify_finger(self):
        for game in self.ongoing_games:
            self.run_command('t 101 SB Monthly 2015 game in progress: '
                '{white} ({white_rank}) vs. {black} ({black_rank}) '
                '-- Round {round} "observe {game_no}'.format(**game))
            self.clock_statistician.processing = False


    def send_channel_messages(self):
        message = ("Interested in playing slow time control tournaments on FICS? "
                   "Check out https://www.snailbucket.org !")
        self.run_command('t 101 %s' % message)
        self.run_command('t 90 %s' % message)


    def on_login(self, my_username):
        print('I am logged as %s, use "tell %s help" to start conversation on '
            'FICS' % (my_username, my_username))

        self._gamenotify_task = task.LoopingCall(self._notify_finger)
        self._gamenotify_task.start(1200, now=False)

        self._channel_notify_task = task.LoopingCall(self.send_channel_messages)
        self._channel_notify_task.start(3600, now=False)


        # Normal post-login processing
        return defer.DeferredList([
                self.set_finger(FINGER_TEXT)
                ])

    @defer.inlineCallbacks
    def on_fics_unknown(self, what):
            try:
                print(f"GAME STARTED!! {what!r}")
                x = yield self.clock_statistician.get_game_data(
                    str(what.black_name))
                if (str(what.game_spec.game_type).lower() == "standard" and
                                    x[0].lower() == str(what.white_name).lower() and
                                    x[1].lower() == str(what.black_name).lower() and
                                    x[2] == (str(what.game_spec.clock.base_min) + " " + str(what.game_spec.clock.inc_sec))):
                    curr_game = {
                                    'white': str(what.white_name),
                                    'white_rank': what.white_rating,
                                    'black': str(what.black_name),
                                    'black_rank': what.black_rating,
                                    'game_no': what.game_no,
                                    'round': x[3]
                    }
                    self.run_command(f't 101 SB Monthly 2 game has started: '
                                                 f"{curr_game['white']}({curr_game['white_rank']}) vs. "
                                                 f"{curr_game['black']}({curr_game['black_rank']}) -- "
                                                 f"Round {curr_game['round']} 'observe {curr_game['game_no']}' to watch")
                    self.start_observing_game(curr_game['game_no'])

                    self.ongoing_games.append(curr_game)
            except Exception as e:
                print(f"GAME START FAILED!! {what!r}")
                print(e)



    @defer.inlineCallbacks
    def on_game_finished(self, game):
        x = yield self.clock_statistician.get_game_data(str(game.black_name))
        self.run_command(
                                                "t 101 SB Monthly 2 game has ended: %s vs. %s -- Round %d: %s {%s}" %
                                                (game.white_name.name, game.black_name.name, x[3], game.result, game.result_desc))
#         self.clock_statistician.updateGameStatus(
#             game.white_name.name, "2014-11-27 14:00:05")

        if "lost connection" not in game.result_desc:
            time.sleep(15)
            req = urllib.request.Request(  # Use 'urllib.request.Request' instead of 'urllib.Request'
                'https://snailbucket.org/tourney/updateforums/monthly2:R' +
                str(x[3]) + '_' + str(game.white_name.name) + '-' +
                str(game.black_name.name)
            )
            response = urllib.request.urlopen(req)  # Use 'urllib.request.urlopen' instead of 'urllib.urlopen'
            he_page = response.read()

        for gm in self.ongoing_games:
            if gm['white'] == game.white_name.name and gm['black'] == game.black_name.name:
                self.ongoing_games.remove(gm)
                break

        try:
            self.run_command("-gnotify %s" % str(game.black_name))
        except Exception as e:
            print(f"Failed to remove gnotify for black player {game.black_name.name}: {e}")


    def on_logout(self):
        if hasattr(self, '_gamenotify_task'):
            self._gamenotify_task.stop()
            del self._gamenotify_task


################################################################################
# Script argument processing
################################################################################

# TODO: --silent with no logging except errors

options, remainders = getopt.getopt(
    args = sys.argv[1:], shortopts=[], longopts=["debug", "creds="])

if "--debug" in [name for name,_ in options]:
    logging_level = logging.DEBUG
else:
    #logging_level = logging.WARN
    logging_level = logging.INFO

logging.basicConfig(level=logging_level)

################################################################################
# Startup glue code
################################################################################

# TODO: convert back to reconnecting


dbpool = adbapi.ConnectionPool(
    "mysql.connector",
    user=DB_USER,
    password=DB_PASSWORD,
    database=DB_DATABASE,
    host=DB_HOST
)

clock_statistician = SnailBot(dbpool)
my_bot = MyBot(clock_statistician)
reactor.connectTCP(
    FICS_HOST, FICS_PORT,
    ReconnectingFicsFactory(
        client=my_bot,
        auth_username=FICS_USER, auth_password=FICS_PASSWORD))
#noinspection PyUnresolvedReferences
reactor.run()




