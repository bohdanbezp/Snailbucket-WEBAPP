# -*- coding: utf-8 -*-

"""
Generic support for finding shortcut command and parameter names.
"""

import logging
from mekk.fics.tell_commands import tell_errors
import six

logger=logging.getLogger("fics.lib")

class ShortcutResolver(object):
    """
    Resolving partial (incomplete) names and aliases.

    Used to resolve command names and parameters in FICS style, where
    we can write "fi" to call "finger", or "acc" instead of "accept",
    and also "=" instead of "showlist".

    The objects works in a simple way: in constructor we register
    list of allowed keywords and their aliases. Then the resolve
    method can be used to find the best match.
    """

    def __init__(self, keywords=None, aliases=None, force_lowercase=True):
        """
        Constructs the resolver. Example:

        cmd_resolver = ShortcutResolver(
            keywords=["listplayers", "listgames", "play",
                      "register", "withdraw"],
            aliases={"lp": "listplayers",
                     "lg": "listgames",
                     "submit": "register"})

        There is no need to define aliases for prefixes
        unless they are non-unique (for example in the example
        above "listp" will be automatically resolved as "listplayers",
        but "list" will be ambiguous unless defined as an alias
        (and even if "list" is defined as an alias, "lis" is still
        treated as ambiguous).

        Additional keywords and aliases can be added
        after constructing the object by means of add_keyword
        and add_alias methods, if incremental way of constructing
        the object is more appropriate.

        :param keywords: list of keywords (canonical command/param names), empty by default
        :type keywords: [str]
        :param aliases: additional aliases (map aliases to appropriate
            canonical names), empty by default
        :type aliases: {str:str}
        :param force_lowercase: should keywords, aliases, and resolved strings
            be automatically converted to lowercase? True by default.
        :type force_lowercase: bool
        """
        self.force_lowercase = force_lowercase
        # Maps keyword/alias → keyword
        self._keywords={}
        if keywords:
            for keyword in keywords:
                self.add_keyword(keyword)
        if aliases:
            for alias, keyword in six.iteritems(aliases):
                self.add_alias(alias, keyword)

    def _add_keyword_mapping(self, alias, keyword):
        prev = self._keywords.get(alias)
        if prev is not None:
            if prev != keyword:
                raise tell_errors.ShortcutKeywordConflict(
                    "Attempt to define %s as alias to %s while it is already defined as %s" % (
                        alias, keyword, prev))
        self._keywords[alias] = keyword

    def add_keyword(self, keyword):
        """
        Adds the word to the list of keywords. Adding the same keyword
        twice is allowed (and has no effect), adding keyword which was
        previously defined as an alias results in the exception.
        """
        if self.force_lowercase:
            keyword=keyword.lower()
        self._add_keyword_mapping(keyword, keyword)

    def add_alias(self, alias, keyword):
        """
        Adds alias as an alias to keyword. The keyword must already
        be defined as keyword and alias must not be defined as alias
        or keyword - violations to those rules are reported as exceptions.
        """
        if self.force_lowercase:
            alias=alias.lower()
            keyword=keyword.lower()
        if not keyword in self._keywords:
            raise tell_errors.ShortcutAliasToUnknownKeyword(
                "Attempt to define %s as alias to non-existant keyword %s" % (
                    alias, keyword))
        self._add_keyword_mapping(alias, keyword)

    def resolve(self, text):
        """
        Finds the best matching keyword for the text. First looks
        for exact matching keywords and aliases, if not found, looks
        if it is a prefix of just one.
        """
        if self.force_lowercase:
            text=text.lower()
        # Exact match
        if text in self._keywords:
            return self._keywords[text]
        # Prefix search
        matches = [
            (alias, keyword)
            for alias, keyword in six.iteritems(self._keywords)
            if alias.startswith(text)
            ]
        if len(matches) == 1:
            return matches[0][1]
        elif len(matches) > 1:
            # It is still possible, that all matches point to the same keyword
            # (we have prefix for both keyword and this very keyword alias)
            matched_keywords=set(keyword for _, keyword in matches)
            if len(matched_keywords) == 1:
                return matched_keywords.pop()
            raise tell_errors.ShortcutAmbiguousKeyword(
                text, [alias for alias, _ in matches])
        else:
            #logger.warn("Unknown command: %s. Known commands: %s" % (
            #        t, ", ".join(self.commands)))
            raise tell_errors.ShortcutUnknownKeyword(text, self.list_keywords())

    def list_keywords(self, skip_aliases=False):
        """
        Returns all keywords and aliases, or just keywords
        """
        if not skip_aliases:
            return self._keywords.keys()
        else:
            return sorted([keyword
                    for alias, keyword in six.iteritems(self._keywords)
                    if alias==keyword])
