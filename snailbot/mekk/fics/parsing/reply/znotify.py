# -*- coding: utf-8 -*-

"""
Parsing znotify
"""

from mekk.fics import errors
from mekk.fics.datatypes import ZnotifyInfo, IdleInfo, PlayerName
import re

# pylint: disable=invalid-name

re_tracked_empty = re.compile(
    r"^No one from your notify list is logged on")

re_tracked_non_empty = re.compile(
    r"Present company on your notify list:")

re_tracking_empty = re.compile(
    r"^No one logged in has you on their notify list")

re_tracking_non_empty = re.compile(
    r"The following players have you on their notify list")

re_player_line = re.compile(r"^ \s \s \s+ .* $", re.VERBOSE)
re_player = re.compile(
    r"""^
    (?P<name>[a-zA-Z0-9_]+)
    (?: [(] idle: (?P<idle>\d+) m [)] )?
    $""", re.VERBOSE)


def _parse_player_names(line, full_text):
    items = line.strip(" \n").split(" ")
    result = []
    for item in items:
        m = re_player.search(item)
        if m:
            result.append(IdleInfo(
                name=PlayerName(m.group('name')),
                idle=60 * int(m.group('idle') or 0)))
    return result

def parse_znotify_reply(reply_text):
    """
    Parses complete znotify reply

    :param reply_text: reply text (multiline)
    :return: ZnotifyInfo object
    """
    tracked = []
    tracking = []

    lines = [l for l in reply_text.split("\n") if l]

    ln = lines.pop(0)
    if re_tracked_non_empty.search(ln):
        while lines and re_player_line.search(lines[0]):
            tracked.extend(_parse_player_names(lines.pop(0), reply_text))
    elif re_tracked_empty.search(ln):
        pass
    else:
        raise errors.ReplyParsingException(reply_text, "znotify")

    ln = lines.pop(0)
    if re_tracking_non_empty.search(ln):
        while lines and re_player_line.search(lines[0]):
            tracking.extend(_parse_player_names(lines.pop(0), reply_text))
    elif re_tracking_empty.search(ln):
        pass
    else:
        raise errors.ReplyParsingException(reply_text, "znotify")

    return ZnotifyInfo(tracked=tracked, tracking=tracking)
