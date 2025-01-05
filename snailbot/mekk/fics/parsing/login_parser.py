# -*- coding: utf-8 -*-

"""
Detecting login/password prompts, and lines telling about succesfull login
"""

from mekk.fics.datatypes.player import PlayerName
import re

re_login = re.compile('([Uu]ser|[Ll]ogin):')
re_password = re.compile('[Pp]assword:')
re_guestlogin = re.compile(r"Press return to enter the (?:server|FICS) as \"([^\"]*)\"")
re_normallogin = re.compile(r"Starting FICS session as ([a-zA-Z0-9]+)")

def is_login_prompt(line):
    """
    Checks whether given line looks like login prompt
    :param line: text obtained from FICS
    """
    if re_login.search(line):
        return True
    else:
        return False

def is_password_prompt(line):
    """
    Checks whether given line looks like password prompt
    :param line: whatever FICS provided as after getting login
    """
    if re_password.search(line):
        return True
    else:
        return False

def is_authorized_login_confirmation(line):
    """
    Checks whether given text looks like succesfull login
    confirmation. If so, returns full user name
    (as PayerName object), if not, returns None
    :rtype : PlayerName
    """
    m = re_normallogin.search(line)
    if m:
        return PlayerName(m.group(1))
    else:
        return None

def is_guest_login_confirmation(line):
    """
    Checks whether given text looks like successful guest login
    confirmation. If so, returns full (guest) user name, if not, returns None.
    Note Enter must be sent to confirm such login.
    :rtype : bool
    """
    m = re_guestlogin.search(line)
    if m:
        return PlayerName(m.group(1))
    else:
        return None


