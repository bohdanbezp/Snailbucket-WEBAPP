# -*- coding: utf-8 -*-

"""
Parsing games list.
"""

from mekk.fics import errors
from mekk.fics.datatypes.game_clock import GameClock
from mekk.fics.datatypes.game_info import ExaminedGame, SetupGame, PlayedGame, GameSpec, GamesInProgress
from mekk.fics.datatypes.game_type import GameType
from mekk.fics.datatypes.player import PlayerName
from mekk.fics.parsing.common import numeric_rank
import re

#  2 (Exam.    0 LectureBot     0 LectureBot) [ uu  0   0] W:  1
#  8 (Exam. 1830 Myopic      1890 BULLA     ) [ br  3   0] W:  1
#109 (Exanm. 1860 Zepphire    2028 GriffyJr  ) [ sr 15   0] W: 38
#  4 ++++ yetis       ++++ GuestWWJX  [ bu  5  12]   1:09 -  3:39 (28-26) W: 19
# 14 ++++ DeathValzer ++++ GuestMLSG  [ uu  0   0]   0:00 -  0:00 (34-11) B: 26
# 40 ++++ GuestSRLN   ++++ GuestKKTF  [ bu 10   0]   9:35 -  9:41 (39-39) W:  6
# 51 ++++ SupraPhonic ++++ GuestTWMQ  [ Su  3   0]   2:59 -  2:52 (14-15) B:  4
# 52 ++++ bozziofan   ++++ GuestBFRV  [ su 20   0]  13:33 - 15:41 ( 3-19) W: 39
# 86 ++++ sooon       ++++ GuestZQCS  [ bu  2  12]   1:22 -  2:03 (38-33) B: 16
# 53 ++++ Wampum      1172 nurp       [ bu  5   0]   4:02 -  3:39 (37-34) B: 15
# 36 1447 zzzzzztrain ++++ GuestBVXT  [ bu  2  12]   1:36 -  2:25 (35-36) W: 10
# 71  832 paratoner    912 stshot     [ br 10   0]   5:47 -  7:28 (14-22) W: 20
# 83 1013 origamikid   841 drmksingh  [ br  3   5]   0:23 -  0:44 (27-33) W: 25
# 78 ++++ aGALERA     1866 Ventura    [ bu  3   5]   2:54 -  0:31 (39-38) B: 11
# 44  913 Veeber      1013 LorenzoDV  [pbr 12   0]   5:13 -  3:01 (17-25) W: 31
#  6  875 Rezzone     1066 julianozn  [ br  3   0]   2:42 -  2:53 (36-39) B:  5
# 84  892 ChesssForDu 1054 BouncedChe [ br  5   0]   4:59 -  4:57 (39-39) B:  3
# 34 1118 JaronGroffe  868 sklenar    [ br 10   0]   4:41 -  6:41 (14-11) W: 29
# 85 1254 rugs         823 MjollnirPa [ br  5   3]   1:37 -  0:35 (29-20) W: 30
# 60  908 jailhousero 1244 kurosawa   [ br  2  12]   4:01 -  0:57 (31-31) B: 19
#  7 1098 Belofte     1108 drednought [ br  8   0]   7:32 -  6:56 (39-39) W: 12
# 27 1074 gaoutte     1176 bobsnelle  [ br  3   0]   3:00 -  3:00 (39-39) W:  1
# 77 1399 pratima      896 ibnusina   [ br 10   0]   6:13 -  7:03 (38-38) B: 12
# 37 1120 saxs        1183 PaanEater  [ br  2  15]   4:57 -  4:49 ( 9-16) B: 37
# 90 1266 maryus      1063 cristig    [ br  2  12]   2:59 -  2:58 (25-25) B: 25
# 22 1025 LOVERBOYS   1325 ELysthieri [ br  2  12]   2:25 -  2:01 (39-39) B:  4
# 75 1030 cantoche    1341 Cenerentol [ br 10   0]   7:15 -  8:36 (36-38) W: 15
# 81 1146 riaz        1264 imagoodboy [ br  2  12]   3:21 -  0:40 (33-27) B: 20
# 30 1320 WoodBored   1242 joedt      [ su 15   0]   9:28 -  7:11 (32-30) B: 19
# 24 1395 histo       1312 Deelio     [pbr  3   0]   1:21 -  2:15 (19-20) B: 21
# 59 1308 nolemotion  1425 stevemm    [ br  5   0]   3:10 -  2:19 (31-32) W: 22
# 25 1588 neselov     1190 pavlo      [ br  5   0]   4:58 -  5:00 (39-39) B:  2
# 67 1095 PlatinumKni 1735 LiquidEmpt [pbr  2  12]   3:34 -  2:07 (21-24) W: 34
# 96 1332 Brixhamite  1515 Bobk       [ br  5   0]   3:02 -  2:13 (17-18) B: 31
# 17 1531 luko        1468 rasbauman  [ bu  2  13]   3:12 -  3:22 (38-35) B: 19
# 31 1473 KeresPaul   1544 stamburro  [ bu  5   0]   3:51 -  2:45 (27-29) B: 22
# 18 1406 csund       1643 JeneRoi    [ sr 15   0]  14:49 - 14:54 (38-38) W:  5
# 39 1486 hozz        1613 RestInPeac [ lr  1   0]   0:04 -  0:15 (16-11) B: 39
# 89 1645 beejai      1498 mckinz     [ lr  1   0]   0:58 -  0:59 (39-39) W:  5
# 87 1155 naveenmatha 2056 AEGC       [ sr 15  60]  22:41 - 12:58 (30-37) B: 12
#  1 1700 yacc        1542 dontrookba [psr 20  20]  28:42 - 23:38 (17-19) B: 32
# 42 1608 monteleo    2545 Topolino   [ sr 15   2]  14:34 - 13:48 (38-35) B:  8
# 46 1941 PaulvanDyk  2302 vladx      [ sr 30  10]  20:59 - 28:48 (38-35) B:  7
#
#  97 games displayed.
re_games_gameline = re.compile(r'''
^\s*
(?P<game_no>\d+)
\s+
(?P<white_rank>\d+|\+{4}|\-{4})
\s+
(?P<white>\w+)
\s+
(?P<black_rank>\d+|\+{4}|\-{4})
\s+
(?P<black>\w+)
\s*
\[
(?P<is_private>[ p])
(?P<variant>\w)
(?P<is_rated>[ru])
\s*
(?P<clock_base>\d+)
\s+
(?P<clock_inc>\d+)
\]
''', re.VERBOSE)

re_games_exaline = re.compile(r'''
^
\s*
(?P<game_no>\d+)
\s*
\(Exa
''', re.VERBOSE)

re_games_setup = re.compile(r'''
^
\s*
(?P<game_no>\d+)
\s*
\(Setup
''', re.VERBOSE)

re_games_summary = re.compile('^\s*(?P<count>\d+) games displayed\.$')

def parse_games_reply_line(text):
    """
    Parses single line belonging to the "games" command reply.
    Mostly used by parse_games_reply (see below).

    If the row was parsed as note about running game, returns
    pair

    'Game', PlayedGame object
         (the latter contains appropriate information)

    If it is the examine/analysis, returns pair

    'Examine', ExaminedGame object

    For manually setup games, returns:

    'Setup', SetupGame object

    For final line ('260 games displayed') returns

    'Summary', count (int)

    For unknown (and non-empty) line raises exception
    """
    m = re_games_gameline.match(text)
    if m:
        return 'Game', PlayedGame(
            game_no=int(m.group('game_no')),
            white_truncated_name=PlayerName(m.group('white'), can_be_truncated=True),
            black_truncated_name=PlayerName(m.group('black'), can_be_truncated=True),
            white_rating_value=numeric_rank(m.group('white_rank')),
            black_rating_value=numeric_rank(m.group('black_rank')),
            game_spec=GameSpec(
                game_type=GameType(m.group('variant')),
                clock=GameClock(base_in_minutes=int(m.group('clock_base')),
                                inc_in_seconds=int(m.group('clock_inc'))),
                is_rated=(m.group('is_rated')=='r'),
                is_private=(m.group('is_private')=='p'),
            ))
    m = re_games_exaline.match(text)
    if m:
        return 'Examine', ExaminedGame(game_no=int(m.group('game_no')))
    m = re_games_setup.match(text)
    if m:
        return 'Setup', SetupGame(game_no=int(m.group('game_no')))
    m = re_games_summary.match(text)
    if m:
        return 'Summary', int(m.group('count'))
    if text.strip(" \r\n"):
        raise errors.ReplyParsingException(text, "games")
    return None

def parse_games_reply_gen(text):
    """
    Parses whole reply to the "games" command.

    Works as a generator (wrap with list(...) to convert to list)
    and yields information about all games found. Every item
    is a pair name, detail, one of:

    - 'Game', PlayedGame object
    - 'Examine', ExaminedGame object
    - 'Setup', SetupGame object
    - 'Summary', count (int)
    """
    for line in text.split("\n"):
        if not line:
            continue
        p = parse_games_reply_line(line)
        if p:
            yield p

def parse_games_reply(reply_text):
    """
    Parses whole reply to the "games" command and constructs
    single object reresenting the information gathered.

    :param reply_text: reply text (multiline)
    :return: GamesInProgress object
    """
    items = dict(Game=[], Examine=[], Setup=[], Summary=[])
    for what, data in parse_games_reply_gen(reply_text):
        items[what].append(data)
    if items['Summary']: # summary presence suggest proper structure
        return GamesInProgress(
            games = items['Game'],
            examines = items['Examine'],
            setups = items['Setup'])
    else:
        raise errors.ReplyParsingException(reply_text, "games")
