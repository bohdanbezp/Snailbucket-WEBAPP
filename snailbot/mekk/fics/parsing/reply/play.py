# -*- coding: utf-8 -*-

"""
Parsing game-related commands replies.
"""

import re
from mekk.fics import errors

re_not_playing = re.compile('^You are not playing a game\.')

def parse_made_move_reply(reply_text):
    # TODO: good replies
    """
    Parse output of move-related command
    :param reply_text: FICS text
    """
    if re_not_playing.search(reply_text):
        raise errors.AttemptToActOnNotPlayedGame(reply_text)
    return None


def parse_assess_reply(reply_text):
    """
    Reply to assess command
    :param reply_text: FICS text
    """
    # TODO: good replies
    if re_not_playing.search(reply_text):
        raise errors.AttemptToActOnNotPlayedGame(reply_text)
    return None
