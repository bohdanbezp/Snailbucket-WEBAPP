# -*- coding: utf-8 -*-

"""
Parsing finger output.
"""

import decimal
import re
from mekk.fics import errors
from mekk.fics.datatypes.player import FingerInfo, PlayerRating, ResultStats
from mekk.fics.datatypes.game_type import GameType

# Finger structure:
# - intro (Finger of…)
# - presence (Last disconnected/On for…/? - probably also never connected etc)
# - optional info about observing or playing in ()
# - optional rating table
# - optional admin level
# - optional Timeseal (not present for not loged)
# - optional plan, possibly broken into lines
# Strict parsing of a whole is likely to bring errors and corner-cases, therefore
# we try to cherry-pick parts instead.
re_finger_reply_intro = re.compile('''
^Finger\s+of\s+(?P<name>\w+)(\([^()]+\))*:\s*$
''', re.VERBOSE)
re_finger_reply_rating_intro = re.compile('''
^\s+rating\s+RD\s+win\s+loss\s+draw\s+total(?:\s+best)?\s*$
''', re.VERBOSE)
re_finger_reply_rating_line = re.compile('''
^
(?P<kind>\w+)
\s+
(?P<rating>\d+)
\s+
(?P<rd>\d+\.\d+)
\s+
(?P<win>\d+)
\s+
(?P<loss>\d+)
\s+
(?P<draw>\d+)
\s+
(?P<total>\d+)
(?:
  \s+
  (?P<best>\d+)
  \s+
  \((?P<bestdate>\d+-\w+-\d+)\)
)?
''', re.VERBOSE)

re_finger_reply_plan_line = re.compile('''
^
(?P<no>\s[0-9]|10):\s
(?P<text>.*)
$
''', re.VERBOSE)

re_no_such_player = re.compile("""
    ^
    (?:
    There\sis\sno\splayer\smatching\sthe\sname\s(?P<name1>\w+)
    |
    '(?P<name2>\w+)'\sis\snot\sa\svalid\shandle
    )
    """, re.VERBOSE)


def parse_finger_reply(reply_text):
    """
    Parses output of finger command, extracting some selected information

    Extract the following fields:

    - name - player name
    - stats - dictionary of statistics for games which given player
              plays. Possible keys (only the appropriate are present,
              in some cases stats dict can be empty): Blitz, Standard,
              Lightning, Wild, Bughouse, Crazyhouse, Suicide, Atomic,
              Losers, Bughouse. For every key, the value is next
              dictionary, with fields (all with integer values except rd which is decimal.Decimal):
              rating, rd, win, loss, draw, best (best is optional,
              the rest is present always)
    - plan - array of (up to 10) strings - finger text

    :param reply_text:
    :return: dictionary of fields, as described above
    """

    # TODO: this is rather clumsy code, consider using true parser for this case

    reply_lines = [line
                    for line in reply_text.strip(" \r\n").split("\n")
                    if line]
    if not reply_lines:
        raise errors.ReplyParsingException(reply_text, "finger")
    # Intro line with user name (or error of no such player)
    first_line = reply_lines.pop(0)
    m = re_finger_reply_intro.match(first_line)
    if not m:
        m = re_no_such_player.match(first_line)
        if m:
            raise errors.UnknownPlayer(m.group('name1') or m.group('name2'))
        raise errors.ReplyParsingException("finger lead: '%s'" % first_line, "finger")
    name = m.group('name')
    results = dict()
    plan = []
    while reply_lines:
        line = reply_lines.pop(0)
        if re_finger_reply_rating_intro.match(line):
            while reply_lines:
                m = re_finger_reply_rating_line.match(reply_lines[0])
                if not m:
                    break
                info = ResultStats(
                    rating = PlayerRating(
                        value = int(m.group('rating')),
                        rd = decimal.Decimal(m.group('rd'))),
                    wins_count = int(m.group('win')),
                    draws_count = int(m.group('draw')),
                    losses_count = int(m.group('loss')),
                    best = (m.group('best') and int(m.group('best')) or None),
                )
                game_type = GameType(m.group('kind'))
                results[game_type] = info
                reply_lines.pop(0)
            break
        elif re_finger_reply_plan_line.match(line):
            # accounts with no game played have no ratings at all. While registered
            # users have line "somebody has not played any rated game", guests may
            # have completely nothing. So we look for first plan line as a signal that
            # there will be no ratings.
            reply_lines.insert(0, line)
            break
    while reply_lines:
        #logger.debug("DROPME: line %s" % reply_lines[0])
        m_plan = re_finger_reply_plan_line.match(reply_lines.pop(0))
        if m_plan:
            while m_plan:
                #logger.debug("DROPME: is plan line")
                plan.append( (int(m_plan.group('no')), m_plan.group('text')) )
                if reply_lines:
                    #logger.debug("DROPME: line %s" % reply_lines[0])
                    m_plan = re_finger_reply_plan_line.match(reply_lines.pop(0))
                else:
                    m_plan = None
            break
    # Ensure ordering
    for idx, no in enumerate(no for no, text in plan):
        if idx+1 != no:
            raise errors.ReplyParsingException(
                "Wrong numbering of plan lines (%s):\n%s" % (",".join(str(no) for no,_ in plan), reply_text),
                "finger")
    return FingerInfo(
            name=name,
            results=results,
            plan=[text for no, text in plan])
