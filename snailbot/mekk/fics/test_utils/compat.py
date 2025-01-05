# -*- coding: utf-8 -*-

"""
Helper routines for (unit) tests.

Those routines are not used, and not intended to be used, in the actual code,
but turned out to be useful in unit-tests not only of this module, but also of
some derived modules/scripts. Therefore they are kept as source code.

This module contains (limited) support for running tests both under
trial and under nose.
"""

# Proper SkipTest depends on whether we run under trial, or under nose.
# Let's hack it
import sys
if sys.argv[0].endswith("trial"):
    from twisted.trial.unittest import SkipTest as TrialSkipTest
    SkipTest = TrialSkipTest
else:
    from nose import SkipTest as NoseSkipTest
    SkipTest = NoseSkipTest
