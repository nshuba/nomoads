#!/usr/bin/python

#  This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
#  Copyright (C) 2018, 2019 Anastasia Shuba
#
#  NoMoAds is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  NoMoAds is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.

import pytest, os, sys

# Test-specific imports
import hashlib, json, copy

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + os.sep + "..")
from utils.pii_helper import PIIHelper

@pytest.fixture
def pii_helper():
    pii_dict = {
        "Test ID": "some_id",
        "Another ID": "123456789"
    }
    location_coords = [("33.64", "-117.84"), ("33.6", "-117.8")]

    return PIIHelper(pii_dict, location_coords)

@pytest.fixture
def test_data():
    with open("test_file_pii_helper.json", "r") as jf:
        return json.load(jf)

@pytest.fixture
def expected_data():
    with open("expected_file_pii_helper.json", "r") as jf:
        return json.load(jf)

def test_no_pii(pii_helper, test_data, expected_data):
    redacted_data, pii_found = pii_helper.get_pii_from_data(test_data["no_pii"])

    assert not pii_found
    assert redacted_data == expected_data["no_pii"]

def test_pii_in_header(pii_helper, test_data, expected_data):
    original_data = copy.deepcopy(test_data["pii_in_header"])
    redacted_data, pii_found = pii_helper.get_pii_from_data(original_data)

    assert len(pii_found) == 1
    assert "Test ID" in pii_found
    assert redacted_data == expected_data["pii_in_header"]
    assert original_data == test_data["pii_in_header"]

def test_pii_in_url(pii_helper, test_data, expected_data):
    original_data = copy.deepcopy(test_data["pii_in_url"])
    redacted_data, pii_found = pii_helper.get_pii_from_data(original_data)

    assert len(pii_found) == 1
    assert "Another ID" in pii_found
    assert redacted_data == expected_data["pii_in_url"]
    assert original_data == test_data["pii_in_url"]

def test_pii_in_header_and_url(pii_helper, test_data, expected_data):
    original_data = copy.deepcopy(test_data["pii_in_header_and_url"])
    redacted_data, pii_found = pii_helper.get_pii_from_data(original_data)

    assert len(pii_found) == 2
    assert "Test ID" in pii_found
    assert "Another ID" in pii_found
    assert redacted_data == expected_data["pii_in_header_and_url"]
    assert original_data == test_data["pii_in_header_and_url"]

def test_location_in_header_and_url(pii_helper, test_data, expected_data):
    original_data = copy.deepcopy(test_data["location_in_header_and_url"])
    redacted_data, pii_found = pii_helper.get_pii_from_data(original_data)

    assert len(pii_found) == 1
    assert PIIHelper.PII_KEY_LOCATION in pii_found
    assert redacted_data == expected_data["location_in_header_and_url"]
    assert original_data == test_data["location_in_header_and_url"]

def test_location_and_other_pii(pii_helper, test_data, expected_data):
    original_data = copy.deepcopy(test_data["location_and_other_pii"])
    redacted_data, pii_found = pii_helper.get_pii_from_data(original_data)

    assert len(pii_found) == 2
    assert PIIHelper.PII_KEY_LOCATION in pii_found
    assert "Another ID" in pii_found
    assert redacted_data == expected_data["location_and_other_pii"]
    assert original_data == test_data["location_and_other_pii"]
