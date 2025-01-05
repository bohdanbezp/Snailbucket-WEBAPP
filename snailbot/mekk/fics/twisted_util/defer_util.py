# -*- coding: utf-8 -*-

"""
Additional support for deferred handling.
"""

import logging
from twisted.internet import defer, reactor, task

logger = logging.getLogger("defer_util")

def delay_succeed(reply, delay=0.01):
    """
    Similar to defer.succeed(reply), but introduces some minimal
    delay before replying. Also, cancel's itself in case deferred
    is cancelled.

    This is most useful in tests, where it allows one to simulate
    mocked replies as truly async - but can also be used to force
    delays in processing.

    Note: this is a simpler version of twisted.internet.task.deferLater
    """
    def cancelCallLater(deferred):
        delayed_call.cancel()
    d = defer.Deferred(cancelCallLater)
    delayed_call = reactor.callLater(delay, d.callback, reply)
    return d

def delay_exception(exception, delay=0.01):
    """
    Provides deferred which will fail after given delay with given
    exception.

    This is most useful in tests, where it allows one to simulate
    mocked replies as truly async - but can also be used to force
    delays in processing.
    """
    def cancelCallLater(deferred):
        delayed_call.cancel()
    d = defer.Deferred(cancelCallLater)
    delayed_call = reactor.callLater(delay, d.errback, exception)
    return d

class CancellingDeferredList(defer.DeferredList):
    """
    Update to normal DeferredList: any failure causes all unfinished
    deferreds on the list to be cancelled.

    This is particularly useful when combined with task.deferLater,
    delay_succeed, delay_exception and similar "clean properly after being cancelled"
    deferreds, as it cleans the reactor from delayed calls on first failure without
    the need to wait for all results (of which some may never arrive).

    - DeferredList(fireOnOneErrback=true) leaves reactor dirty as nobody
      awaits other results
    - DeferredList(fireOnOneErrback=false) may hang if some deferred
      is awaiting for failed one
    - CancelingDeferredList fails immediately and properly cleans up everything
      (as long as deferreds in use have proper cancellers)

    Note: errback to it is not called, callback gets usual list of [(status, value-or-failure)]
    items. See gather_with_cancel below for easier to use results.
    """

    def __init__(self, deferredList):
        self.managed_deferreds = deferredList[:]
        defer.DeferredList.__init__(
            self, deferredList,
            fireOnOneCallback=False, # of course
            fireOnOneErrback=False,  # all will be finished or cancelled and we wait for all of them
            consumeErrors=True,     # otherwise we get all those „Unhandled error in Deferred” errors for cancels etc.
            )

    def _cbDeferred(self, result, index, succeeded):
        if not succeeded:
            for pos, dfr in enumerate(self.managed_deferreds):
                if pos != index and not dfr.called:
                    dfr.cancel()
        result = defer.DeferredList._cbDeferred(self, result, index, succeeded)
        return result

def gather_with_cancel(deferred_list):
    """
    Alternative to defer.gatherResults with "sync all" guarantee.

    See CancellingDeferredList for semantics, this function
    additionally:

    - in OK case provides results as simple list,
    - in failed case throws first exception and logs other exceptions
    """
    if not deferred_list:
        return defer.succeed([])

    d_list = CancellingDeferredList(deferred_list)

    def parse_results(lst):
        failures = [value for is_success, value in lst if not is_success]
        if failures:
            # Szukamy błędu do rzucenia, będzie to pierwszy nie-cancel chyba
            # że są same cancele
            non_cancel_failures = [ f for f in failures
                                    if not f.check(defer.CancelledError)]
            # Throwing exception
            if non_cancel_failures:
                for ignored_fail in non_cancel_failures[1:]:
                    # TODO: maybe better logging
                    logger.warning(
                        "Swallowing exception as another exception is forwarded to the caller",
                        exc_info=(
                            ignored_fail.type,
                            ignored_fail.value,
                            ignored_fail.getTracebackObject()))
                    logger.exception(ignored_fail.value)
                non_cancel_failures[0].raiseException()
            else:
                failures[0].raiseException()
        # Skoro nie było błędów...
        return [value for is_success, value in lst ]

    d_list.addCallback(parse_results)

    return d_list
