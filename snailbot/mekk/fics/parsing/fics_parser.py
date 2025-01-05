# -*- coding: utf-8 -*-

"""
FICS stream consumer.
"""

from mekk.fics.parsing import info_parser
from mekk.fics.parsing.reply.block_mode_filter import BlockModeFilter
from mekk.fics.parsing.reply_parser import parse_fics_reply
import logging

logger = logging.getLogger("fics.lib")


class FicsTextProcessor(object):
    """
    Integrated parser for text arriving from FICS. Is fed
    with every line obtained from FICS, parses it (including
    handling block mode, i.e. wrapped command replies), skips
    empty lines and ignorable information and calls appropriate
    callbacks depending on the input type.

    This class does NOT handle communication (must be fed with input
    received) and does NOT handle login stage (should be activated
    after login is over and block mode is set).
    """
    def __init__(self,
                 info_callback,
                 block_callback,
                 label,
                 fics_prompt='fics% '):
        """
        Initialize the object, saving callbacks.

        :param info_callback: the callable to be called on every piece of
            FICS-initiated information. It gets two parameters:

            - event_type ('tell', 'game_move', 'user_connected', ...)
            - event parameters (object, dictionary or tuple, depending
               on the event_type).

            See `mekk.fics.parsing.info_parser.parse_fics_line`
            for the list of known types and structure of appropriate params.

        :param block_callback: the callable to be called whenever reply
            to some command is obtained. It gets four parameters:

            - command id (integer identifier used to correlate the reply
              with issued command, it is equal to id assigned to command
              while sending it),
            - command name ('games', 'date', ...),
            - status (True when we succesfully executed and parsed reply,
              False in case of problems)
            - data (appropriate command data on success, exception on failure)

            See `mekk.fics.parsing.info_parser.parse_fics_reply` for the list
            of possible command names and types of data associated to them.

        :param label: textual label used during logging (usually some
            name/identifier of FICS connection, important if program
            maintains a few simultaneous connection as then it allows to
            distinguish between them)

        :param fics_prompt: configured fics prompt (this string is to be skipped)
        """
        self.block_filter = BlockModeFilter(
            block_callback=self._handle_reply,
            fics_prompt=fics_prompt)
        self.info_callback = info_callback
        self.block_callback = block_callback
        self.label = label

    def consume_input_line(self, input):
        """
        Consumes received line of text, calling appropriate callbacks
        when necessary.

        :param input: input line (possibly, but not necessarily, with newline)
        :return: List of replies of all callback called at this time (not interpreted
           by this class, often those are deferreds fired once processing is finished)
        """
        line, outputs = self.block_filter.handle_line_noting_callbacks(input)
        line = line.strip("\r\n")
        what, params = info_parser.parse_fics_line(line)
        if what != "ignore":
            if what == "unknown":
                # Until obtaining first prompt we ignore unknowns, FICS sends plenty of announcements after login
                # which need to be skipped (it does not make sense to carefully parsing them)
                if self.block_filter.prompt_seen():
                    logger.warn("%s: FICS line not yet recognized by the parser: %s" % (self.label, line))
                    outputs.append(self.info_callback(what, params))
                else:
                    logger.debug("%s: ignored unknown(%s)" % (self.label, line))
            else:
                logger.debug("%s: %s(%s)" % (
                    self.label, what, params))
                outputs.append(self.info_callback(what, params))
        return outputs

    def _handle_reply(self, command_id, command_code, reply_text):
        command_name, status, command_data = parse_fics_reply(
            command_code, reply_text)
        logger.debug("%s: handling command reply (id:%d, type: %d, name:%s, successful: %s): %s" % (
            self.label, command_id, command_code, command_name, str(status), str(command_data)))
        return self.block_callback(command_id, command_name, status, command_data)
