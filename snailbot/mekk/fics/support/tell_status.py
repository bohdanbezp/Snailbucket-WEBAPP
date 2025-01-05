# -*- coding: utf-8 -*-

"""
Avoiding telling many error warnings to the same player.
"""

class TellLoopPrevention(object):
    """
    Keeps track of tells exchanged with other players to detect cases of bot tell loops
    (when two bots inform one another about wrong command or similar error).

    The bot logic calls:

    - good_tell method when given user sends proper non-empty command (so he or she
      is noted as proper partner for communication)

    - bad_tell method when given user sends wrong or empty tell. This method returns
      whether he or she should be told about the error.

    The object allows no more than max_errors_allowed successive errors.
    """
    def __init__(self, max_errors_allowed):
        """
        Object construction
        :param max_errors_allowed: how many errors per user are allowed (one more and we stop responding)
        """
        self._bad_command_count = dict()   # nick -> count of errors since last good tell
        self._max_errors_allowed = max_errors_allowed

    def good_tell(self, who):
        """
        Given user said sth properly.
        :param who: player whose tell was proper
        """
        if who in self._bad_command_count:
            del self._bad_command_count[ who ]

    def bad_tell(self, who):
        """
        Given user said sth incorrect.

        :returns should he/she be told about an error? True if so, False if he should be ignored
                 (as this is next error in sequence and we risk a loop if we keep talking)
        """
        count = self._bad_command_count.get(who, 0) + 1
        self._bad_command_count[who] = count
        return count <= self._max_errors_allowed

# TODO: monitor tell frequency, detect people talking too often
