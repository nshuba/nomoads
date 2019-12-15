#!/usr/bin/python

"""
Run with pytest:
$ pip install pytest
"""
#   This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
#   Copyright (C) 2018 Anastasia Shuba, University of California, Irvine.
#
#   NoMoAds is free software: you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation, either version 2 of the License, or
#   (at your option) any later version.
#
#   NoMoAds is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.

import sys, os, json

from adblockparser import AdblockRules
from urlparse import urlsplit

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + os.sep + "..")
from data_anal import filter_list_checker

def test_detect_content():
    url = "http://example.com/tracker.js"
    assert filter_list_checker.get_content_type(urlsplit(url)) == filter_list_checker.type_script

    url = "http://example.com/tracker.jsextra"
    assert filter_list_checker.get_content_type(urlsplit(url)) != filter_list_checker.type_script

def test_get_origin():
    url = "https://example.com/app2"
    assert filter_list_checker.get_origin(urlsplit(url)) == "httpsexample.com"

    url = "http://example.com:8080/app2"
    assert filter_list_checker.get_origin(urlsplit(url)) == "httpexample.com:8080"

def test_isxmlreq_isthirdparty():

    url = "tpc.googlesyndication.com/pagead/"
    assert filter_list_checker.isxmlreq_isthirdparty(urlsplit(url), {}) == (False, False)

    headers = {
        "X-Requested-With": "XMLHttpRequest"
    }
    assert filter_list_checker.isxmlreq_isthirdparty(urlsplit(url), headers) == (True, False)

    headers = {
        "Referer": "https://googleads.g.doubleclick.net/"
    }
    assert filter_list_checker.isxmlreq_isthirdparty(urlsplit(url), headers) == (False, True)

    headers["X-Requested-With"] = "XMLHttpRequest"
    assert filter_list_checker.isxmlreq_isthirdparty(urlsplit(url), headers) == (True, True)

def test_get_block_decision():
    rules = AdblockRules(['||example.com'])
    with open("test_file.json", "r") as jf:
        data = json.load(jf)

        assert not filter_list_checker.get_block_decision(rules, data["no_host"])
        assert filter_list_checker.get_block_decision(rules, data["host_only"])
        assert filter_list_checker.get_block_decision(rules, data["everything"])

        third_party_rule = AdblockRules(['||crashlytics.com^$third-party'])
        assert filter_list_checker.get_block_decision(third_party_rule, data["third_party_block"])
