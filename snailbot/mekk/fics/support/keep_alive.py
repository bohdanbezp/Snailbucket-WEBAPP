# -*- coding: utf-8 -*-

"""
Periodical calling of unnecessary command to avoid timeouts.
"""

import logging
import time
import sys
from twisted.internet import defer, task, reactor
import six

# pylint: disable=invalid-name,line-too-long,broad-except

logger = logging.getLogger("fics.lib")

###########################################################################

# TODO: if we use activate_timeout_checker on block deferreds,
# checking the commands here for timeout looks like
# duplicating the effort. Still, as keepalive command
# is simple, shorter timeout may be used than used there.


class KeepAliveExecutor(object):
    """
    Calling some „keepalive” command (for example - „date”) at regular
    intervals to ensure connection is not dropped due to inactivity.
    Also - monitors whether the reply is obtained in reasonable time,
    if not, reports the problem.

    To be used in context of Twisted reactor.

    Example usage::

        keep_alive = KeepAliveExecutor(
            command=lambda: protocol.run_command("date"),
            on_failure=protocol.on_keepalive_failure,
            label="Worker 3",
            frequency=10 * 60,
            timeout=2 * 60)

    (and it will just work until the object is deleted, or .stop() is
    called)
    """

    ############################################################
    # Standardowe API użytkowe
    ############################################################

    def __init__(self, label, frequency, timeout,
                 command, on_failure,
                 on_critical_failure=None, on_success=None,
                 start_immediately=False):
        """
        Initializes and starts the keepalive monitoring.

        :param label: text label for logging (like worker/connection
            name)
        :param frequency: how often is keepalive executed - in
            seconds, fractions like 0.5 can be used (in FICS
            connection context sensible values are from a 120-180 (2-3
            minutes) - when fast detection of connection freeze is
            necessery - to 1800-2400 (30-45 minutes) - when our only
            concern is to avoid disconnection due to inactivity
        :param timeout: max time (in seconds, floats acceptable) we
            wait for keepalive reply.  Can be 0 or None, then we check
            whether the reply appeared before issuing the next
            keepalive
        :param command: callable called to execute keepalive command.
            Gets no parameters, should return Deferred called when
            command gets reply (or erred when it fails)
        :param on_failure: callable called in case keepalive command
            fails or times out.  Gets no parameters. Can be called
            multiple times.
        :param on_critical_failure: callable called in case some gross
            error happens.  In such case the object is no longer
            guaranteed to work sensibly and the process is likely in
            bad shape.  By default executes reactor.stop() to finish
            the process.
        :param on_success: callable called in case of successful
            keepalive (executed without errors and finished on time).
            Can be (and is by default) None, then nothing is called.
        :param start_immediately: should we make first call straight
            away (True), or after frequency passes (False, default)
        """

        self._command = command
        self._on_failure = on_failure
        self._on_success = on_success
        self._on_critical_failure = on_critical_failure
        self._label = label
        self._frequency = frequency
        if timeout and (timeout > 0) and (timeout < 0.8 * frequency):
            self._timeout = timeout
        else:
            self._timeout = 0

        # call no. Used to identify calls
        self._call_no = 0
        # pending (not yet finished) calls. Map:
        #     call_no -> {
        #        start_time: time,
        #        dfr: deferred wrapping actual spawned command,
        #     }
        self._pending = dict()
        # Twisted object responsible for waking us up at appropriate moments.
        self._task = task.LoopingCall(self._wakeup)
        # Result of callLater executed to perform verify_result call. Can be
        # None if we don't use that (no timeout)
        self._pending_result_check = None

        # Call statistics
        self._ok_count = 0
        self._fail_count = 0

        logger.info("%s: Activating keepalive tracking, frequency %d, timeout %d",
                    self._label, frequency, self._timeout)
        # Control for initiation task
        self._task_mon = self._task.start(frequency, now=start_immediately)

    def __del__(self):
        self.stop()

    def stop(self, completion_delay=0.01):
        """
        Stops the object, immediately cancels any monitoring efforts
        (also done automatically in the destructor).

        Returns deferred fired on completion

        :param completion_delay: artificial delay injected into returned
             deferred in case we cancel works in progress (to make more
             likely they actually finish)
        """
        if self._task:
            logger.info(
                "%s: Stopping keepalive counter after %d calls. Successes: %d, failures: %d, still pending (to be cancelled): %d",
                self._label, self._call_no, self._ok_count, self._fail_count, len(self._pending))
            wait_for = [self._task_mon]
            self._task.stop()
            self._task = None
            self._task_mon = None
            # Don't cancel this one, it will fire as the result of pending
            # deferred cancel's (and keeping it makes syncing over them easier)
            # if self._pending_result_check:
            #     if self._pending_result_check.active():
            #         self._pending_result_check.cancel()
            if self._pending:
                for item in list(self._pending.values()):
                    dfr = item['dfr']
                    dfr.cancel()
                    wait_for.append(dfr)
                # Here we happen to finish too soon, and sync-ing perfectly
                # on cancel is not possible, so let's give it sme heuristical
                # time to finish (and avoid dirty reactor)
                wait_for.append(
                    task.deferLater(reactor, completion_delay, lambda: 0))
            if len(wait_for) == 1:
                return wait_for[0]
            else:
                return defer.gatherResults(wait_for, consumeErrors=1)
        else:
            return defer.succeed(None)

    ############################################################
    # Internal methods
    ############################################################

    def _wakeup(self):
        """
        Keepalive wakeup.  Calls test command, registers callbacks to
        store the result and to verify result arrival.
        """
        try:
            # If we use no separate timeout, we check for prev results here
            if not self._timeout:
                self._verify_result_arrival()
            # Maybe we are already stopped?
            if not self._task:
                return

            self._call_no += 1
            logger.info("%s: Executing keepalive call no %d",
                        self._label, self._call_no)
            now = time.time()
            dfr = self._command()
            assert isinstance(dfr, defer.Deferred)
            self._pending[self._call_no] = {'start_time': now, 'dfr': dfr}

            def _callback(dt, call_no):
                "Keepalive callback, note success"
                if call_no in self._pending:
                    info = self._pending[call_no]
                    del self._pending[call_no]
                    age = time.time() - info['start_time']
                    if self._timeout and age > self._timeout:
                        logger.info(
                            "%s: Keepalive call %d succeded but arrived too late, output %s",
                            self._label, call_no, dt)
                        self._handle_failure()
                    else:
                        logger.info("%s: Keepalive call %d succeded, output %s",
                                    self._label, call_no, dt)
                        self._handle_success()

            def _errback(failure, call_no):
                "Keepalive callback, note error"
                if call_no in self._pending:
                    info = self._pending[call_no]
                    del self._pending[call_no]
                    logger.error(failure)
                    self._handle_failure()

            dfr.addCallback(_callback, self._call_no)
            dfr.addErrback(_errback, self._call_no)

            if self._timeout:
                if (not self._pending_result_check) or (not self._pending_result_check.active()):
                    self._pending_result_check = reactor.callLater(
                        self._timeout, self._verify_result_arrival)

        except Exception as e:
            logger.exception("%s: Exception during keepalive wakeup: %s",
                             self._label, str(e))
            self._handle_critical_failure()

        return None

    def _verify_result_arrival(self):
        """
        Called 'timeout' after keepalive call, checks whether issued
        command finished on time and reports problem in case it did
        not.
        """
        now = time.time()
        if self._timeout:
            time_limit = now - self._timeout
        else:
            time_limit = now - self._frequency

        # In case of heavy load there is some likelihood this callback is delayed and we see „new”,
        # not yet timeouted one request. Therefore we check whether the time really passed.
        lagging_items = [
            (call_no, issued)
            for call_no, issued in six.iteritems(self._pending)
            if issued['start_time'] <= time_limit]

        if lagging_items:
            if len(lagging_items) == 1:
                issued = lagging_items[0][1]['start_time']
                logger.fatal(
                    "%s: No reply to previous keepalive command issued at %s (%s sec. ago). Frozen connection? Cancelling check to signal failure",
                    self._label, time.ctime(issued), str(now-issued))
            else:
                logger.fatal(
                    "%s: No reply to %d previous keepalive commands issued at %s. Frozen connection? Cancelling check to signal failure",
                    self._label,
                    len(lagging_items),
                    ", ".join(str(issued['start_time']) for _, issued in lagging_items))
            for _, item in lagging_items:
                item['dfr'].cancel()

        # We don't call on_failure here, it will happen due to cancel


    def _handle_success(self):
        self._ok_count += 1
        if self._on_success:
            try:
                self._on_success()
            except:
                logger.exception("%s: on success handler failed", self._label)

    def _handle_failure(self):
        "Failure handling: call given callback, handle errors"
        self._fail_count += 1
        try:
            self._on_failure()
        except Exception:
            logger.exception(
                "%s: on_failure handler crashed, promoting to critical failure",
                self._label)
            self._handle_critical_failure()

    def _handle_critical_failure(self):
        "Critical failure handling: call user callback, or stop reactor"
        try:
            if self._on_critical_failure:
                self._on_critical_failure()
            else:
                if reactor.running:
                    reactor.stop()
        except Exception:
            logger.exception(
                "Error from critical failure callback. Exiting process")
            sys.exit(1)
