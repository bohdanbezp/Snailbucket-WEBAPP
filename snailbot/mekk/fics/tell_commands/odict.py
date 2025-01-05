# -*- coding: utf-8 -*-

"""
Import best possible OrderedDict
"""

try:
    from collections import OrderedDict
except ImportError:
    # pip install ordereddict
    from ordereddict import OrderedDict

