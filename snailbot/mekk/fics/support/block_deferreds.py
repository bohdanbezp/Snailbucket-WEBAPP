# -*- coding: utf-8 -*-

"""
Allocating identifiers for FICS commands and binding results to requests.
"""

from mekk.fics import errors
from twisted.internet import defer, reactor, task
import logging, datetime

logger = logging.getLogger("fics.lib")

class BlockDeferreds(object):
    """
    Allocate block-mode command identifiers (unique numbers), assign deferreds to them,
    and remember them until the command reply is received.

    Additionally, preserves the information about the time commands were issued, making
    it easier to detect timeouts.

    Single object of this class is used internally by every fics connection to manage
    association between issued commands and replies to them.
    """
    def __init__(self, label):
        """
        Initiates (empty) object

        :param label: some textual label of the connection. Used for logging.
        :type label: str
        """
        self._label = label
        # Table of „commands in progress”, maps integer id to appropriate tracking
        # data. Every element of this table can be:
        # - None if id is available
        # - a pair (initiation_time, deferred) if command is in progress
        # - a pair (initiation time, None) if we treat command as timed out (such
        #   ids remain allocated to detect cases of very late reply)
        self._reply_deferreds = []
        # Statistical info
        self._completed_count = 0
        # Twisted task responsible for awaking timeout detection
        self._timeout_detector_task = None

    def __del__(self):
        self.reset()

    def allocate(self):
        """
        Called when some new command is to be issued.

        Allocates some id for this command, and some deferred which will
        serve as promise of the command reply. Remembers association between the two.

        :returns: (id, d) - id for the command, deferred for callback
        :rtype: (int, defer.Deferred)
        """
        # Looking for spare place in used range
        l = len(self._reply_deferreds)
        id = l
        for i in range(0, l):
            if not self._reply_deferreds[i]:
                id = i
                break
        d = defer.Deferred()
        t = reactor.seconds() # TODO: or maybe time.time()?
        if id == l:
            self._reply_deferreds.append( (t,d) )
        else:
            self._reply_deferreds[id] = (t,d)
        return id+1, d

    def force(self, identifier, deferred):
        """
        Alternative to allocate, enforces given deferred under given id.

        Not intended for normal use, useful in trascript-replay unit tests.
        """
        pos = int(identifier) - 1
        while len(self._reply_deferreds) <= pos:
            self._reply_deferreds.append(None)
        self._reply_deferreds[pos] = (reactor.seconds(), deferred)

    def fire(self, identifier, value):
        """
        Called after reply to command identified by identifier is obtained.

        Fires deferred associated with given id, giving value as it's parameter,
        and removes it from the list. Do nothing if given deferred is missing.

        :param identifier: command id (usually earlier allocated via allocate)
        :type identifier: int
        :param value: parameter to be given to the deferred (usually command reply)
        :returns: d - fired deferred (or empty deferred if nothing happened)
        :rtype: defer.Deferred
        """
        reply_deferred = self._capture_deferred_for(identifier)
        #logger.debug("%s: CommandReply(%s, %s)" % (self._label, identifier, value))
        if reply_deferred is not None:
            reply_deferred.callback(value)
            return reply_deferred
        else:
            return defer.succeed(None)

    def fire_error(self, identifier, failure):
        """
        Called after command identified by identifier fails.

        Errbacks deferred associated with given id with the failure obtained,
        and removes it from the list. Log error if given deferred is missing.

        :param identifier: command id
        :param failure: exception object describing command failure
        :return: fired deferred (or empty deferred if nothing happened)
        """
        reply_deferred = self._capture_deferred_for(identifier)
        #logger.debug("%s: FicsCommandException(%s, %s)" % (self._label, identifier, str(failure)))
        if reply_deferred is not None:
            reply_deferred.errback(failure)
            return reply_deferred
        else:
            return defer.succeed(None)

    def _capture_deferred_for(self, identifier):
        """
        Takes deferred for identifier from the list and returns it
        :param identifier: command id
        :return: deferred associated to this command, or None if it is missing
        """
        assert isinstance(identifier, int), "command id should be int, but is %s" % str(identifier)
        pos = identifier - 1
        self._completed_count += 1
        if pos >= 0:
            pos_info = self._reply_deferreds[pos]
            self._reply_deferreds[pos] = None
            if pos_info:
                alloc_time, reply_deferred = pos_info
                if reply_deferred is None:
                    logger.warn("%s: reply to command %d appeared too late (after %s seconds), ignoring" % (
                        self._label, identifier, reactor.seconds() - alloc_time))
                return reply_deferred
        return None

    def reset(self):
        """
        Called on disconnect or reconnect.

        Drops all pending deferreds (and logs info about them).
        """
        self.deactivate_timeout_checker()
        left = [ (pos+1, d)
                 for pos, d in enumerate(self._reply_deferreds)
                 if d ]
        if left:
            logger.info("%s: Throwing away hanging commands: %s" % (self._label, str(left)))
        self._reply_deferreds = []
        self._completed_count = 0

    def active_count(self):
        """
        Returns the number of commands currently active.
        """
        return sum(1 for item in self._reply_deferreds if item and item[1])

    def completed_count(self):
        """
        Returns the number of commands successfully executed since the object creation.
        """
        return self._completed_count

    # TODO: użyć
    # TODO: także sprawdzanie jak dawno było cokolwiek
    def detect_timed_out(self, timeout_in_seconds):
        """
        Checks for all active commands to which replies failed to be obtained, errbacks them
        and marks as timed out.

        Note: activate_timeout_checker method allows one to initiate automatical check
        at regular intervals.

        :param timeout_in_seconds: how old unanswered commands are considered timed out
        :type timeout_in_seconds: float
        :return: list of all deferreds impacted (can be used to sync finish with their finish)
        """
        timed_out_deferreds = []
        curr_time = reactor.seconds()
        for i in range(0, len(self._reply_deferreds)):
            v =  self._reply_deferreds[i]
            if v is not None:
                start_time, dfr = v
                if dfr is not None:
                    delay = curr_time - start_time
                    if delay > timeout_in_seconds:
                        timed_out_deferreds.append(dfr)
                        self._reply_deferreds[i] = (start_time, None)
                        dfr.errback(errors.FicsCommandTimedOut(
                            elapsed_time=datetime.timedelta(seconds=delay)))
                            #"Command %d failed to get reply in %s seconds" % (i, str(delay))))
        return timed_out_deferreds

    def activate_timeout_checker(self,
                                 check_frequency_in_seconds,
                                 timeout_in_seconds,
                                 on_timeouts):
        """
        Activates periodical checking for timed-out commands. Check is executed
        every check_frequency_in_seconds seconds. In case there are timeouts:

        - every timed-out deferred is errbacked with `CommandTimedOut` exception,
        - on_timeouts callable is called (once, whatever was the count of timed out commands).

        :param check_frequency_in_seconds: how often should this check be executed (usual
            sensible value is sth like timeout/4 or so - this is the max time by which we may
            exceed the timeout before detecting it)
        :param timeout_in_seconds: longest allowed time for executing a command
        :param on_timeouts: Callable which will be called (without parameters)
            if any timeouts happened. If None, nothing is called.
        """
        if self._timeout_detector_task:
            raise errors.LibraryUsageException("Timeout checker is already active")

        def wakeup_timeout_detector(timeout, call_on_timeouts):
            dlist = self.detect_timed_out(timeout)
            if dlist:
                if call_on_timeouts:
                    r = call_on_timeouts()
                    if isinstance(r, defer.Deferred):
                        dlist.append(r)
                # Such return would suspend checks until we are done with cleaning up
                # the current mess (task.LoopingCall convention). It is not really worth it,
                # hanging timeout handler would stop us completely.
                #return defer.DeferredList(dlist)

        self._timeout_detector_task = task.LoopingCall(
            wakeup_timeout_detector,
            timeout_in_seconds,
            on_timeouts)
        self._timeout_detector_task.start(check_frequency_in_seconds, now=False)

    def deactivate_timeout_checker(self):
        """
        Deactivates timeout checker (activated via acivate_timeout_checker) - if active.
        """
        if self._timeout_detector_task:
            self._timeout_detector_task.stop()
            self._timeout_detector_task = None
