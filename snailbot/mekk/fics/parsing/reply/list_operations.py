# -*- coding: utf-8 -*-

"""
Parsing channel, player etc lists.
"""

import re
from mekk.fics import errors
from mekk.fics.datatypes.list_items import ListContents
from mekk.fics.datatypes.player import PlayerName

# -- channel list: 2 channels --
# 4   53
#  (items can be multiline if there are m# -- channel list: 2 channels --
# 4   53
#  (items can be multiline if there are many)

re_showlist = re.compile('''
\A
\s*--\s*
(?P<name>\w+)
\s+
list:
\s+(?P<count>\d+)\s+[^\-]*
\s*--\s*
(?P<items>.*)
\Z
''', re.VERBOSE + re.DOTALL)

re_list_split = re.compile('[\s\r\n]+')

re_list_error = re.compile(r'"(?P<list>\w+)" does not match any list name\.')

def parse_showlist_reply(reply_text):
    m = re_showlist.match(reply_text)
    if m:
        name = m.group('name')
        items = re_list_split.split(m.group('items').strip(" \r\n"))
        if items and items[-1] == '':  # split('') -> ['']
            items.pop(-1)
        return ListContents(name=name, items=items)
    m = re_list_error.match(reply_text)
    if m:
        raise errors.UnknownList(m.group('list'))
    else:
        raise errors.ReplyParsingException(reply_text, "showlist")

re_handles = re.compile('''
\A
\s*--\s*
Matches:\s*
(?P<count>\d+)\s+[^\-]*
\s*--\s*
(?P<items>.*)
\Z
''', re.VERBOSE + re.DOTALL)

re_handles_error_syntax = re.compile(r"'(?P<name>[^']+)' is not a valid handle\.")
re_handles_error_missing = re.compile(r'^There is no player matching the name (?P<name>\w+)')

def parse_handles_reply(reply_text):
    m = re_handles.match(reply_text)
    if m:
        items = re_list_split.split(m.group('items').strip(" \r\n"))
        if items and items[-1] == '':  # split('') -> ['']
            items.pop(-1)
        return ListContents(name="handles", items=[PlayerName(item) for item in items])
    m = re_handles_error_missing.match(reply_text)
    if m:
        raise errors.UnknownPlayer(m.group('name'))
    m = re_handles_error_syntax.match(reply_text)
    if m:
        raise errors.UnknownPlayer(m.group('name'))
    raise errors.ReplyParsingException(reply_text, "handles")

