# -*- coding: utf-8 -*-

"""
Helper routines for (unit) tests.

Those routines are not used, and not intended to be used, in the actual code,
but turned out to be useful in unit-tests not only of this module, but also of
some derived modules/scripts. Therefore they are kept as source code.
"""

import os, logging
import six
log = logging.getLogger(__name__)

try:
    from dict_compare import dict_compare
    def assert_dicts_equal(testobj, received, expected):
        """
        Slightly better alternative to self.failUnlessEqual(dict1, dict2) if dict_compare
        is installed, just self.failUnlessEqual(dict1,dict2) if not.
        :param testobj: unittest.TestCase object (aka self inside test method)
        :param received: obtained value
        :param expected: expected value
        """
        testobj.assertTrue( dict_compare(expected, received, reporter=testobj.fail) )
except ImportError:
    #noinspection PyUnusedLocal
    def dict_compare(*args,**kwargs):
        """
        Fake dict_compare
        """
        raise Exception("dict_compare not installed")

    def assert_dicts_equal(testobj, received, expected):
        """
        Fail if two dictionaries are not equal. Give some details in such a case.
        :param testobj: unittest object (the one to call .failUnlessEqual on)
        :param received: what we got
        :param expected: what we wanted
        """
        testobj.failUnlessEqual(expected, received)

def assert_tables_equal(testobj, received, expected):
    for left, right in zip(received, expected):
        testobj.failUnlessEqual(left, right)
    testobj.failUnlessEqual(len(received), len(expected))
    testobj.failUnlessEqual(received, expected)

import pkg_resources
def load_tstdata_file(subdir, name, package="mekk.fics"):
    """Loads file of given name from given subdir of tests
    directory. Tries hard to locate it in various environments."""

    possible_data_dirs = []

    # Wdg. formalnej lokalizacji pakietu
    try:
        package_dir = pkg_resources.get_distribution(package).location
        data_dir = os.path.join(os.path.dirname(package_dir), "tests", subdir)
        possible_data_dirs.append(data_dir)
    except pkg_resources.DistributionNotFound:
        log.warn("Can't find mekk.fics distribution, install is suspicious (using /usr/bin/nosetests for virtualenv installed package?)")

    for cand in ["..", ".", "../.."]:
        data_dir = os.path.join(cand, "tests", subdir)
        if os.path.exists(data_dir):
            possible_data_dirs.append(os.path.abspath(data_dir))

    for pdd in possible_data_dirs:
        pdd_name = os.path.join(pdd, name)
        if os.path.exists(pdd_name):
            return load_file_content(pdd, name)

    raise Exception(
        """Can not locate test data file (tests/%s/%s).
Tried the following directories:
    %s
        """ % (subdir, name, "\n    ".join(possible_data_dirs)))
    

    return load_file_content(data_dir, name)
    #candidate_dirs = [
    #    x % subdir
    #    for x in [ "%s", "tests/%s", "../tests/%s", "../%s" ] ]
    #return load_file_content(
    #    pick_existing_dir(*candidate_dirs), name)

def pick_existing_dir(*dirs):
    """
    Pick and return first existing dir from all given as params
    """
    for cand in dirs:
        try:
            os.stat(cand)
            return cand
        except OSError:
            pass
    raise Exception("Can not find test data, none of the dirs exist: " + \
                    ", ".join(dirs) + (" (current dir: " + os.getcwd() + ""))

def load_file_content(location, name):
    """
    Shortcut for loading complete file content in one step.
    """
    if six.PY3:
        with open(os.path.join(location, name), "r", 
                  encoding="iso-8859-1", newline="\n") as fileobj:
            return fileobj.read(-1)
    else:
        with open(os.path.join(location, name), "r") as fileobj:
            return fileobj.read(-1)
