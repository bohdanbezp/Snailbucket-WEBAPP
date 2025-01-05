# -*- coding: utf-8 -*-

"""
Enforcing delays between successive commands.
"""

# pylint: disable=invalid-name,line-too-long

import time
from twisted.internet import reactor, defer
import logging

logger = logging.getLogger('fics.lib')


class DelayingExecutor(object):
    """
    Ensures minimal intervals between execution of some commands (mainly used
    for sending commands to FICS, but implemented in general way).

    Assumes working in context of Twisted reactor.

    Typical use:

       de = DelayingExecutor(interval=0.1, command=protocol.sendLine, label="Conn 03")
       de.execute("date")
       de.execute("set 1 blah blah")
       de.execute("set 2 bleh bleh")
       # Commands are executed with at least 0.1 second delay between each
    """

    def __init__(self, interval, command, label):
        """
        Initiates the object
        :param interval: minimal delay between successive executions (in seconds, one can use fractions like 0.2)
        :param command: actual callback. Will be given whatever parameters are provided to execute.
        :param label: textual label of object for logging (usually - connection name)
        """
        self._command = command
        self._interval = interval
        self._label = label
        self._last_call = 0   # time.time()
        self._buffer = []
        self._sync_deferreds = []  # deferreds to fire once the buffer goes empty

    def __done__(self):
        self.reset()

    def reset(self):
        """
        Drops whole delayed buffer, the object can be used as new.
        """
        if self._buffer:
            logger.info("%s: Throwing buffered commands without execution:\n%s",
                        self._label, str(self._buffer))
            self._buffer = []
        self._flush_sync_deferreds()

    def execute(self, *args, **kwargs):
        """
        Executes or registers for execution. The command will be called at appropriate moment,
        guaranteeing ordering (late execute's are executed after those issued earlier) and minimal delay.
        """
        if self._interval:
            self._buffer.append((args, kwargs))
            self._flush()
        else:
            self._really_execute(args, kwargs)

    def sync(self):
        """
        Allows one to wait for the completion of all commands issued. Note that callback may never happen if
        execute is kept called at fast rate, and that after callback new commands may be added again.

        The method is used during testing, may also help implement clean shutdown.

        :return: deferred which will be fired (with single argument: True) once there are no waiting commands.
        """
        if self._buffer:
            d = defer.Deferred()
            self._sync_deferreds.append(d)
            return d
        else:
            return defer.succeed(True)

    ###########################################################################
    # Internal methods
    ###########################################################################

    def _really_execute(self, args, kwargs):
        self._command(*args, **kwargs)   # pylint: disable=star-args
        self._last_call = time.time()
        #logger.debug("%s: Execute( %s %s )" % (self._label, str(args), str(kwargs)))

    def _flush(self):
        if self._buffer:
            delta = time.time() - self._last_call
            if delta >= self._interval:
                self._really_execute(* self._buffer.pop(0))
            if self._buffer:
                reactor.callLater(max(0, self._interval - delta), self._flush)
            else:
                self._flush_sync_deferreds()

    def _flush_sync_deferreds(self):
        for d in self._sync_deferreds:
            d.callback(True)
        self._sync_deferreds = []

