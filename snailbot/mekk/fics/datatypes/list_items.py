# -*- coding: utf-8 -*-

"""
Items on the list.
"""

from collections import namedtuple

#noinspection PyClassicStyleClass
class ListContents(namedtuple("ListContents",
    "name, items")):
    """
    Information about some FICS list (=td,
    =computer, =notify etc etc). Has two fields:

    - name (string) - list name ('td', 'notify', ...)
    - items (list of strings) - actual items on the list
    """
    pass
