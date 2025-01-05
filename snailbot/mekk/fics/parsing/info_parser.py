# -*- coding: utf-8 -*-

"""
Parsing different FICS-initiated notifications.
"""

import re
import datetime
import logging
from mekk.fics.datatypes.color import Color
from mekk.fics.datatypes.game_clock import GameClock
from mekk.fics.datatypes.style12 import Style12
from mekk.fics.datatypes.game_info import GameReference, GameSpecPartial, GameSpec
from mekk.fics.datatypes.game_type import GameType
from mekk.fics.datatypes.generic import GenericText
from mekk.fics.datatypes.notifications import GameFinish, AttributedTell, ChannelTell, GameKibitz, GameStart, GameStartExt, GameMove, CompressedMove, GameNote, SeekRef, Seek, GameJoinInfo
from mekk.fics.datatypes.player import PlayerName
from mekk.fics.parsing.common import rated_as_bool, numeric_rank
from mekk.fics.parsing.reply.observe import re_unobserved

logger = logging.getLogger('fics.lib')

# Parsing dispatch table. Below we have triplets:
# - name (as replied by parse_fics_line)
# - regexp to match the line
# - lambda routine which takes match object and returns the info object

PARSE_DISPATCH = [

    # TODO: remove tell Watchbot below 
    #    (after moving to more appropriate place, maybe 
    #     general strip of "tell YourName" in command dispatcher

    # Johny tells you: blah blah
    # Mamer(TD) tells you: bleh bleh
    # Johny(SR)(TM) tells you: blah blah
    ("tell",
     re.compile(r"""
     ^
     (?P<who>[^\s()]+)    # Johny
     (?:\(\S+\))*         # (TD)(TM)(SR) itp
     \stells\syou:\s      #  tells you:
     (?:tell\sWatchBot\s)? # częste pomyłki z wpisaniem tell WatchBot do okna czata
     (?P<what>.*)         # treść
     $
     """, re.VERBOSE),
     lambda m: AttributedTell(
            player=PlayerName(m.group('who')),
            text=m.group('what'))),

     # playerbis(106): ble ble ble
     # GuestKKLX(U)(4): your extremely lucky i just slipped my piece there
    ("channel_tell",
     re.compile(r"""
     ^
     (?P<who>[^\s\)]+)       # Johny
     (?:\(\S+\))*            # (C), (SR)(TD) itp
     \((?P<channel>\d+)\)    # (106)
     :\s                     # :
     (?P<what>.*)            # blah blah
     $
     """, re.VERBOSE),
     lambda m: ChannelTell(
            player=PlayerName(m.group('who')),
            text=m.group('what'),
            channel=int(m.group('channel')))),

     ("qtell",
      re.compile("^:(?P<what>.*)"),
      lambda m: m.group('what')),

    # {Game 1 (playerbis vs. root) Creating rated standard match.}
    # {Game 142 (GuestFQJN vs. GuestCFVZ) Creating unrated blitz match.}
    # {Game 155 (spgs vs. Miklo) Creating rated bughouse match.}
    # {Game 32 (Chussi vs. SquibCakes) Creating rated suicide match.}
    # {Game 165 (ThawCY vs. ChessCracker) Creating rated wild/4 match.}
    # {Game 166 (xufei vs. chessactuary) Continuing rated blitz match.}
    ("game_started",
     re.compile(r"""
     ^
     \{Game\s
     (?P<game_no>\d+)
     \s\(
     (?P<white>[^\s]+)
     \svs.\s
     (?P<black>[^\s]+)
     \)\s
     (?:Creating|Continuing)
     \s
     (?P<is_rated>rated|unrated)
     \s
     (?P<variant>[^\}]+)
     \smatch\.}
     """, re.VERBOSE),
     lambda m: GameStart(
            game_no=int(m.group('game_no')),
            white_name=PlayerName(m.group('white')),
            black_name=PlayerName(m.group('black')),
            game_spec=GameSpecPartial(
                game_type=GameType(m.group('variant')),
                is_rated=rated_as_bool(m.group('is_rated')),
                ))),

    # +gnotify effect: 
    # Game notification: Mlasker (1709) vs. emranhamid (1791) rated standard 15 0: Game 262
    ("game_started_ext",
     re.compile(r"""
     ^
     Game \s+ notification: \s+
     (?P<white>[^\s]+) \s+
     \(\s?(?P<white_rank>[\dPE+-]{3,6})\)               # (2322), (----), (2322E), ( 822)
     \svs.\s
     (?P<black>[^\s]+) \s+
     \(\s?(?P<black_rank>[\dPE+-]{3,6})\)               # (2322), (----), (2322E), ( 822)
     \s+
     (?P<is_rated>rated|unrated)
     \s+
     (?P<variant>[a-z/ ]+[0-9]?)
     \s+
     (?P<base>\d+)\s+(?P<inc>\d+)
     : \s+ Game \s+ (?P<game_no>\d+)
     """, re.VERBOSE),
     lambda m: GameStartExt(
            game_no=int(m.group('game_no')),
            white_name=PlayerName(m.group('white')),
            white_rating=numeric_rank(m.group('white_rank')),
            black_name=PlayerName(m.group('black')),
            black_rating=numeric_rank(m.group('black_rank')),
            game_spec=GameSpec(
                game_type=GameType(m.group('variant')),
                is_rated=rated_as_bool(m.group('is_rated')),
                is_private=False,
                clock=GameClock(base_in_minutes=int(m.group('base')),
                                inc_in_seconds=int(m.group('inc')))))),

    # It shows up when we start game or start observing some game, 
    # not quite game start so we differ it a bit
    ("game_joined",
     re.compile(r"""
     ^
     <g1>
     \s
     (?P<game_no>\d+)
     \s
     p=(?P<private>[01])
     \s
     t=(?P<variant>\S+)
     \s
     r=(?P<rated>[01])
     \s
     u=(?P<white_registered>[01]),(?P<black_registered>[01])
     \s
     it=(?P<white_base>\d+),(?P<black_base>\d+)
     \s
     i=(?P<white_inc>\d+),(?P<black_inc>\d+)
     \s
     pt=(?P<partner_game_no>\d+)
     \s
     rt=(?P<white_rank>[\d]{1,4}[PE]*) ?,(?P<black_rank>[\d]{1,4}[PE]*)
     \s+
     ts=(?P<white_timeseal>[01]),(?P<black_timeseal>[01])
     """, re.VERBOSE),
     # TODO: wykorzystać wszystkie powyższe informacje
     lambda m: GameJoinInfo(
            game_no=int(m.group('game_no')),
            white_rating=numeric_rank(m.group('white_rank')),
            black_rating=numeric_rank(m.group('black_rank')),
            #partner_game
            #white_timeseal
            #black_timeseal
            game_spec=GameSpec(
                game_type=GameType(m.group('variant')),
                clock=GameClock(m.group('white_base'), m.group('white_inc'),
                                m.group('black_base'), m.group('black_inc')),
                is_rated=(m.group('rated') == '1'),
                is_private=(m.group('private') == '1'),
                ))),



    # {Game 164 (CamyC vs. android) Neither player has mating material} 1/2-1/2
    # {Game 173 (android vs. CamyC) CamyC forfeits on time} 1-0
    # {Game 62 (Rasquinho vs. farwest) Rasquinho checkmated} 0-1
    # {Game 126 (SquibCakes vs. Chussi) Chussi resigns} 1-0
    # {Game 74 (Christen vs. Rajan) Christen lost connection; game adjourned} *
    # {Game 39 (Sillopsism vs. sparpas) Game aborted on move 1} *
    # {Game 78 (msparrow vs. Belofte) Game courtesyadjourned by msparrow} *
    # {Game 143 (samthefam vs. NemSiMing) samthefam forfeits on time} 0-1
    # {Game 52 (bububfo vs. Friscopat) Game aborted on move 1} *
    # {Game 192 (electricrook vs. dalf) Game drawn by stalemate} 1/2-1/2
    ("game_finished",
     re.compile(r"""
     ^
     \{Game\s
     (?P<game_no>\d+)
     \s\(
     (?P<white>[^ ]+)
     \svs.\s
     (?P<black>[^ ]+)
     \)\s
     (?P<desc>[^\}]+)
     }\s
     (?P<result>1-0|0-1|1/2-1/2|\*)
     """, re.VERBOSE),
     lambda m: GameFinish(
            game_no=int(m.group('game_no')),
            white_name=PlayerName(m.group('white')),
            black_name=PlayerName(m.group('black')),
            result=m.group('result'),
            result_desc=m.group('desc'))),

     # <12> -----r-- --r-p-kp ----Qnp- ----p--- -------- -----PP- P-q---BP ---R-R-K B -1 0 0 0 0 0 164 CamyC android 0 3 0 26 26 112 107 24 Q/a6-e6 (0:01) Qxe6 0 1 215
    ('game_move',
     re.compile(r"^<12>\s(?P<style12>.*)$"),
     lambda m: GameMove(style12=Style12(m.group('style12')))),

     # <d1> game_index num_half_moves algebraic_string smith_string time_taken_in_ms time_left_in_ms
     # <d1> 2 64 Rxc2 e2c2p 1200 203800
    ('compressed_move',
     re.compile(r"""
     ^
     <d1>\s+
     (?P<game_no>\d+)
     \s+
     (?P<half_moves>\d+)
     \s+
     (?P<algebraic>[KQRNBa-h1-8x=+#]+)
     \s+
     (?P<smith>\S+)
     \s+
     (?P<time_taken>\d+)
     \s+
     (?P<time_left>\d+)
     """, re.VERBOSE),
     lambda m: CompressedMove(game_no=int(m.group('game_no')),
                               half_moves_count=int(m.group('half_moves')),
                               algebraic=m.group('algebraic'),
                               smith=m.group('smith'),
                               time_taken=datetime.timedelta(microseconds=1000*int(m.group('time_taken'))),
                               time_left=datetime.timedelta(microseconds=1000*int(m.group('time_left'))),
                               )
     ),
    
     # Goober(C)(2399)[185] kibitzes: Hello from Crafty v19.19! (2 cpus)
     # Mainflame(C)(2322)[185] whispers: d10 +0.27 c3 Be7 dxe5 Nxe4 Nbd2 Nxd2 Bxd2 O-O Bd3 Nc6 O-O d5 egtb: 0 time: 18.70 nps: 132397
    ('game_kibitz',
     re.compile(r"""
     ^
     (?P<who>[^\s()]+)                    # Goober
     (?:\(\S+\))*                         # (C), (SR)(TD) itp
     \(\s?(?P<rank>[^\s()]+)\)               # (2322), (----), ( 958)
     \[(?P<game_no>\d+)\]                  # [181]
     \s(?P<method>kibitzes|whispers):\s   #  kibitzes:
     (?P<what>.*)                         # blah blah
     $
     """, re.VERBOSE),
     lambda m: GameKibitz(
            game_no=int(m.group('game_no')),
            player=PlayerName(m.group('who')),
            rating_value=numeric_rank(m.group('rank')),
            method=m.group('method'),
            text=m.group('what'))),

     # Game 39: Berke offers a draw.
     # Game 39: radioegg declines the draw request.
     # Game 4: wivawo requests to pause the game.
     # Game 4: Kobac accepts the pause request.
     # Game 4: Game clock paused.
     # Game 4: wivawo requests to unpause the game.
     # Game 4: Kobac accepts the unpause request.
     # Game 4: Game clock resumed.
     # Game 97: rahulchess requests to take back 1 half move(s).
     # Game 97: Memler accepts the takeback request.
     # Game 290: Divljak declines the takeback request.
     # Game 51: daneg requests to abort the game.
     # Game 51: kmhaswad declines the abort request.
     # Game 128: dcwarren accepts the draw request.
    ('game_note',
     re.compile(r'^Game (?P<game_no>\d+): (?P<note>.*\.)$'),
     lambda m: GameNote(
            game_no=int(m.group('game_no')),
            note=m.group('note'))),

    # TODO: this is a command reply, check whether it may happen separately or is it just legacy
    ('observing_finished',
     re_unobserved,
     lambda m: GameReference(int(m.group('game_no')))),

    ("shout",
     re.compile(r"""
     ^
     (?P<who>[^\s()]+)    # Johny
     (?:\(\S+\))*         # (TD)(TM)(SR) itp
     \s
     shouts:\s
     (?P<what>.*)         # treść
     $
     """, re.VERBOSE),
     lambda m: AttributedTell(player=PlayerName(m.group('who')),
                              text=m.group('what'))),

    ("cshout",
     re.compile(r"""
     ^
     (?P<who>[^\s()]+)    # Johny
     (?:\(\S+\))*         # (TD)(TM)(SR) itp
     \s
     c-shouts:
     \s
     (?P<what>.*)         # treść
     $
     """, re.VERBOSE),
     lambda m: AttributedTell(player=PlayerName(m.group('who')),
                              text=m.group('what'))),

    ("it_shout",
     re.compile(r"""
     ^
     -->\s                # -->
     (?P<what>
     (?P<who>[^\s()<>]+)    # Johny
     .*)                  # hates the weather today.
     $
     """, re.VERBOSE),
     lambda m: AttributedTell(player=PlayerName(m.group('who')),
                              text=m.group('what'))),

    # [playerbis has connected.]
    ("user_connected",
     re.compile(r"""
     ^
     \[
     (?P<who>\S+)
     \shas\s
     connected
     \.\]
     """, re.VERBOSE),
     lambda m: PlayerName(m.group('who'))),

    # [playerbis has disconnected.]
    ("user_disconnected",
     re.compile(r"""
     ^
     \[
     (?P<who>\S+)
     \shas\s
     disconnected
     \.\]
     """, re.VERBOSE),
     lambda m: PlayerName(m.group('who'))),

    ("watched_user_connected",
     re.compile(r"""
     ^
     Notification:
     \s+
     (?P<who>\S+)
     \shas\s
     arrived
     \.
     $
     """, re.VERBOSE),
     lambda m: PlayerName(m.group('who'))),

    ("watched_user_disconnected",
     re.compile(r"""
     ^
     Notification:
     \s+
     (?P<who>\S+)
     \shas\s
     departed
     \.
     $
     """, re.VERBOSE),
     lambda m: PlayerName(m.group('who'))),

    ("watching_user_connected",
     re.compile(r"""
     ^
     Notification:
     \s+
     (?P<who>\S+)
     \shas\s
     arrived
     \sand\sisn't\son\syour\snotify\slist
     \.
     $
     """, re.VERBOSE),
     lambda m: PlayerName(m.group('who'))),

    ("watching_user_disconnected",
     re.compile(r"""
     ^
     Notification:
     \s+
     (?P<who>\S+)
     \shas\s
     departed
     \sand\sisn't\son\syour\snotify\slist
     \.
     $
     """, re.VERBOSE),
     lambda m: PlayerName(m.group('who'))),

    # **ANNOUNCEMENT** from relay: FICS is relaying the Swedish Championship. (...)
    ("announcement",
     re.compile(r'^\s*\*+ANNOUNCEMENT\*+\s+from\s+(?P<who>\S+):\s*(?P<text>.*)$'),
     lambda m: AttributedTell(
            player=PlayerName(m.group('who')),
            text=m.group('text'))),

    ("seek",
     re.compile(r"""
     ^
     (?P<who>[^\s()]+)                    # Goober
     (?:\([A-Z]{1,2}\))*                         # (C), (SR)(TD) itp ale nie (++++)
     \s*
     \(\s?(?P<rank>[\dPE+-]{3,6})\)               # (2322), (----), (2322E), ( 822)
     \s+
     seeking
     \s+
     (?P<base>\d+)\s+(?P<inc>\d+)          # 1 2
     \s+
     (?P<rated>rated|unrated)
     \s+
     (?P<variant>[\w/]+)
     \s+
     (\[
     (?P<color>white|black)
     \]\s+)?
# TODO: verify order of those fields
     ((?P<manual>m)\s+)?
     ((?P<formula>f)\s+)?
     \("play\s+
     (?P<seek_no>\d+)
     "\s+to\s+respond
     """, re.VERBOSE),
     lambda m: Seek(
         seek_no=int(m.group('seek_no')),
         player=PlayerName(m.group('who')),
         player_rating_value=numeric_rank(m.group('rank')),
         is_manual=(m.group('manual') and True or False),
         using_formula=(m.group('formula') and True or False),
         color=(m.group('color') and Color(m.group('color')) or None),
         game_spec=GameSpec(
             game_type=GameType(m.group('variant')),
             is_rated=rated_as_bool(m.group('rated')),
             is_private=False,
             clock=GameClock(base_in_minutes=int(m.group('base')),
                             inc_in_seconds=int(m.group('inc')))))),

    # Ads removed: 22 1 119
    ("seek_removed",
     re.compile(r'^Ads removed: *(?P<adlist>[0-9\s]+)'),
     lambda m: [ SeekRef(int(adno)) for adno in m.group('adlist').strip().split() ]),

    ("seeks_cleared",
     re.compile(r'^<sc>'),
     lambda m: GenericText(m.group(0))),

    ("seek_removed",
     re.compile(r'^<sr> (?P<adlist>[0-9\s]+)'),
     lambda m: [ SeekRef(int(adno)) for adno in m.group('adlist').strip().split() ]),

    ("seek",
     re.compile(r"""
     ^
     <sn?>
     \s
     (?P<seek_no>\d+)
     \s
     w=(?P<who>\S+)
     \s
     ti=[\dA-F]{2}     # TODO: 0x1-guest, 0x2-comp, 0x4-GM, 0x8-IM,...
     \s
     rt=(?P<rank>[\d]{1,4}[PE]*)
     \s+
     t=(?P<base>\d+)
     \s
     i=(?P<inc>\d+)
     \s
     r=(?P<rated>[ru])
     \s
     tp=(?P<variant>\S+)
     \s
     c=(?P<color>[WB?])
     \s
     rr=\d+-\d+
     \s
     a=(?P<manual>[tf])
     \s
     f=(?P<formula>[tf])
     """, re.VERBOSE),
     lambda m: Seek(
         seek_no=int(m.group('seek_no')),
         player=PlayerName(m.group('who')),
         player_rating_value=numeric_rank(m.group('rank')),
         is_manual=(m.group('manual') == "f" and True or False),
         using_formula=(m.group('formula') == "t" and True or False),
         color=((m.group('color') in ["W", "B"]) and Color(m.group('color')) or None),
         game_spec=GameSpec(
             game_type=GameType(m.group('variant')),
             is_rated=(m.group('rated')=="r"),
             is_private=False,
             clock=GameClock(base_in_minutes=int(m.group('base')),
                             inc_in_seconds=int(m.group('inc')))))),


    ("auto_logout",
     re.compile(r'''
                ^
                \*{4}
                \s+
                (Auto-logout\sbecause\s.*)
                \s+
                \*{4}
                ''', re.VERBOSE),
     lambda m: GenericText(m.group(1))),

    ("ignore",
     re.compile(r"""
     ^
     (?:
     \(told\s\w+\)    # (told Mekk) itp. Sent outside block mode in case tell was queued for 1 second or so - then this notification arrives separately
     |
     block\sset\.
     |
     \s*
     )
     $
     """, re.VERBOSE),
     lambda m: None),

    # Note: while adding new items here do not forget about fics_client/EventMonitoringClient
]

### TODO
# (zapis wiszących bierek w bughouse, idzie extra po s12)
# <b1> game 124 white [PNR] black [PPR]

# CamyC, whom you are following, has started a game with android.
# You are now observing game 173.
# Game 173: android (1789) CamyC (2021) rated blitz 3 0

# GuestKKDN (++++) seeking 5 0 unrated blitz m ("play 22" to respond)
# paysandu (1395) seeking 5 0 unrated blitz ("play 99" to respond)
#re_seek = re.compile(r"(\S+) \(([+\d]+)\) seeking (\d+) (\d+) (rated|unrated)

# Cmohr stopped examining game 289.
# Game 289 (which you were observing) has no examiners.
### END-TODO

#TODO: convert textual names below to lowercase

def parse_fics_line(line):
    """Parse line of text received from FICS. Does not handle
    command results (including block codes) - those should be
    handled earlier (and class BlockModeFilter may be of help).

    If the text was recognized, returns pair
    (command name, command params object). Otherwise returns None.

    Possible returns (textual name, object type):

    - "tell", AttributedTell
    - "qtell", str
    - "channel_tell", ChannelTell
    - "shout", AttributedTell
    - "cshout", AttributedTell
    - "it_shout", AttributedTell
    - "game_move", GameMove
    - "game_kibitz", GameKibitz
    - "game_note", GameNote
    - "user_connected", PlayerName
    - "user_disconnected", PlayerName
    - "watched_user_connected", PlayerName    (for +notify people)
    - "watched_user_disconnected", PlayerName
    - "watching_user_connected", PlayerName    (for people having me on +notify)
    - "watching_user_disconnected", PlayerName
    - "game_started", GameStart
    - "game_started_ext", GameStartExt   (cases when game is on gnotify)
    - "game_joined", GameJoinInfo
    - "game_finished", GameFinish
    - "observing_finished", GameReference
    - "announcement", AttributedTell
    - "seek", Seek
    - "seek_removed", [SeekRef, SeekRef, ...]

    Special returns:

    "ignore", None
        One of the ignored texts (or just empty line)
    "unknown", GenericText
        Returned when the text is not recognized, makes it possible to workaround
        this library missed features
    """
    line = line.strip("\r\n")  # Just in case, not really needed
    if not line:
        return "ignore", None

    for name, regexp, callback in PARSE_DISPATCH:
        m = regexp.search(line)
        if m:
            return name, callback(m)

    # TODO: review
    # Znaczek Ctrl-G jest tak ogólnie jakimś ogranicznikiem ruchów bodajże ale...
    # na razie się go pozbądźmy
    if line == chr(7):
        return 'ignore', None

    return 'unknown', GenericText(line)






### TODO
#{Game No user1 user2       result clocktime inc rated isprivate gametype
#*qtell kto flaga*    - flaga to czy jest online
#*getgi player white black gameNumber gameTime inc rated isPrivate*
#*getpi player rat1 rat2 rat3 rat4 rat5 rat6*
###




