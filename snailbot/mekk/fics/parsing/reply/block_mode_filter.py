# -*- coding: utf-8 -*-

"""
Splitting FICS stream into normal notifications and command replies.
"""

# pylint: disable=invalid-name

from mekk.fics.constants import block_codes
import re

# blah blah
# \ blah blah
re_continuation = re.compile(r'^\\ *')

# Wynik komendy block
re_block_reply_start = re.compile(
    block_codes.BLOCK_START + r"(\d+)"
    + block_codes.BLOCK_SEPARATOR + r"(\d+)"
    + block_codes.BLOCK_SEPARATOR)
re_block_reply_end = re.compile(block_codes.BLOCK_END)
re_block_reply_complete = re.compile(
    block_codes.BLOCK_START + r"(\d+)"
    + block_codes.BLOCK_SEPARATOR + r"(\d+)"
    + block_codes.BLOCK_SEPARATOR
    + r"(.*?)"
    + block_codes.BLOCK_END)


class BlockModeFilter(object):
    """
    Object used to detect and separate command replies (wrapped
    with so called block-mode markers, see help on iv_block)
    from the stream of incoming data lines. Handles also filtering
    out FICS prompts and merging backslash-splitted lines (the latter
    is currently implemented only in command replies and not 100% full-proof,
    therefore better use iv_nowrap).

    This object is setup once for the connection, configured
    with callback called every time some reply is finished, and fed with
    every line of input via handle_line calls.

    Typical usage::

        blk_filter=BlockModeFilter(block_callback=my_callback,
                                   fics_prompt="fics% ")
        for line in line_input:
            line = blk_filer.handle_line(line)
            # normal line processing. line is stripped from any replies
            # (block regions) and prompt (if present). Note that returned
            # line can be empty.
            #
            # In case some reply is finished, the callback is called
            # with (id, code, reply_text)

    In case replies from callbacks are useful in main-loop processing
    (one use case is to sync on finished callback processing),
    alternate form can be used::

        blk_filter=BlockModeFilter(block_callback=my_callback)
        for line in line_input:
            line, cb_replies = blk_filer.handle_line_noting_callbacks(line)
            # normal line processing. line is stripped from any block regions
            # and prompt (if present).
            #
            # In case some block is finished, callback is called
            # with (id, code, reply_text).
            #
            # cb_replies is list of values returned from called callback(s)
            # (in all practical cases it will be at most 1 value)
    """

    def __init__(self, block_callback, fics_prompt="fics% "):
        """
        Initialize filter object, save callback to be called whenever
        some reply is detected.

        block_callback should be a callable (function, method, callable object)
        accepting 3 parameters:

        :id:
            (`int`) command id (as given while calling, allows one to match
            reply with request),
        :code:
            (`int`) command code (one of block_codes.BLKCMD_ constants),
        :reply_text:
            (`str`) actual text of the reply (can be multiline, with \n
            as separator)

        Parameters:

        :param block_callback: the function to be called whenever
            some command reply is noticed (and gathered).
        :param fics_prompt: FICS prompt in use
        :type block_callback: callable
        :type fics_prompt: str
        """
        self.callback = block_callback
        self._current_id = None
        self._current_code = None
        self._current_text = None
        self._fics_prompt = fics_prompt
        self._prompt_seen = False

    def prompt_seen(self):
        """
        Have we seen fics prompt at least once? The method helps to skip FICS
        post-login announcements.
        :return: True if FICS prompt was processed (skipped) at least once
            during the object life.
        """
        return self._prompt_seen

    def handle_line(self, line):
        """
        Analyze next line of input.  If there are reply blocks in it
        (or reply block started earlier and is not yet over, or reply
        block starts), strips it and calls callback at appropriate
        moment.  Returns remaining („normal”) text

        :param line: next line of text (stripped of any \r or \n)
        :type line: str

        :return: text remaining after command replies and FICS prompt
                 removal (can be empty)
        :rtype: str
        """
        line, _ = self.handle_line_noting_callbacks(line)
        return line

    def handle_line_noting_callbacks(self, line):
        """
        Analyze next line of input.  Extract any reply block(s), call
        callback on them and grab callback return value(s).  Strip the
        rest from FICS prompt (if present).  Returns remaning text (or
        empty string).

        :param line: line of input (without trailing newline)

        :return: (clean line, [callback replies])
        """
        def append_next_line(prev, line):
            m = re_continuation.match(line)
            if m:
                # Join prev and current line together, ensuring a space
                # between them unless it is already present
                if prev.endswith(' '):
                    filler = ''
                else:
                    filler = ' '
                return prev + filler + line[m.end():]
            else:
                return prev + "\n" + line

        def make_callback(ident, code, text):
            return self.callback(ident, code, text)

        cb_replies = []
        # Are we already inside unfinished block? If so, let's try closing it
        if self._current_id:
            end_match = re_block_reply_end.search(line)
            if end_match:
                (ident, code, text) = (
                    self._current_id, self._current_code, self._current_text)
                self._current_id = None
                self._current_code = None
                self._current_text = None
                text = append_next_line(text, line[:end_match.start()])
                line = line[end_match.end():]
                cb_replies.append(
                    make_callback(ident, code, text))
            else:
                self._current_text = append_next_line(self._current_text, line)
                return "", []
        # Grab complete blocks (if any). More than one never happens,
        # but better be safe than sorry.
        while True:
            full_match = re_block_reply_complete.search(line)
            if not full_match:
                break
            ident = int(full_match.group(1))
            code = int(full_match.group(2))
            text = full_match.group(3)
            line = line[:full_match.start()] + line[full_match.end():]
            cb_replies.append(
                make_callback(ident, code, text))
        # Skip prompt if present
        if line.startswith(self._fics_prompt):
            line = line[len(self._fics_prompt):]
            self._prompt_seen = True
        # Note start of block
        start_match = re_block_reply_start.search(line)
        if start_match:
            self._current_id = int(start_match.group(1))
            self._current_code = int(start_match.group(2))
            self._current_text = line[start_match.end():]
            line = line[:start_match.start()]
        return line, cb_replies

