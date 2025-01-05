# -*- coding: utf-8 -*-

"""
List-related parsing.
"""

# pylint: disable=invalid-name

import re
from mekk.fics import errors
from mekk.fics.datatypes.channel import ChannelRef
from mekk.fics.datatypes.player import PlayerName

re_plus_channel = re.compile(
    r'^\[(?P<no>\d+)\] added to your channel list')
re_plus_notify = re.compile(
    r'^\[(?P<who>\S+)\] added to your (notify|gnotify) list')

re_minus_channel = re.compile(
    r'^\[(?P<no>\d+)\] removed from your channel list')
re_minus_notify = re.compile(
    r'^\[(?P<who>\S+)\] removed from your (notify|gnotify) list')

re_err_already = re.compile(
    r'\[(?P<item>[\dA-Za-z_]+)\] is already on your (?P<list>channel|notify|gnotify) list')
re_err_missing = re.compile(
    r'\[(?P<item>[\dA-Za-z_]+)\] is not in your (?P<list>channel|notify|gnotify) list')


def parse_addlist_reply(reply_text):
    """
    Parse reply to +chan, +notify etc.

    :param reply_text: FICS reply
    :type reply_text: str
    """
    # TODO: mayhaps return also list name (channel, notify, gnotify…)?
    m = re_plus_channel.search(reply_text)
    if m:
        return ChannelRef(int(m.group('no')))
    m = re_plus_notify.search(reply_text)
    if m:
        return PlayerName(m.group('who'))
    m = re_err_already.search(reply_text)
    if m:
        raise errors.AlreadyOnList(m.group('item'), m.group('list'))
    return None


def parse_sublist_reply(reply_text):
    """
    Parse reply to -chan, -notify etc.

    :param reply_text: FICS reply
    :type reply_text: str
    """
    m = re_minus_channel.search(reply_text)
    if m:
        return ChannelRef(int(m.group('no')))
    m = re_minus_notify.search(reply_text)
    if m:
        return PlayerName(m.group('who'))
    m = re_err_missing.search(reply_text)
    if m:
        raise errors.NotOnList(m.group('item'), m.group('list'))

    return None
