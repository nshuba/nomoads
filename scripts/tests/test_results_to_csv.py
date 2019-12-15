#!/usr/bin/python

"""
Run with pytest:
$ pip install pytest
$ pytest
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

from collections import Counter

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + os.sep + "..")
from data_anal import results_to_csv
from utils import utils, settings, json_keys

def test_csv_converter():
    settings.init("config.cfg")

    with open("results_to_csv_test_file.json", "r") as jf:
        data = json.load(jf)
        packet = data["all_labels"]
        csv_converter = results_to_csv.CSVconverter(packet)

        # Test header
        expected_header_row = [json_keys.package_name,
                               json_keys.ats_pkg,
                               json_keys.id,
                               json_keys.host,
                               json_keys.domain,
                               json_keys.uri,
                               json_keys.referer,
                               json_keys.headers,
                               json_keys.method,
                               settings.json_key_label,
                               json_keys.pii_label,
                               "predicted_general",
                               "predicted_package_name",
                               "list_labels_easy_list",
                               "list_labels_easy_privacy",
                               "list_labels_moaab"]
        header_row = csv_converter.get_header_row()
        print header_row
        assert Counter(expected_header_row) == Counter(header_row)

        # Test values of row 1
        row = csv_converter.get_row(packet, "all_labels")
        print row
        assert "value_general" == row[header_row.index("predicted_general")]
        assert "value_package_name" == row[header_row.index("predicted_package_name")]
        assert "value_easy_list" == row[header_row.index("list_labels_easy_list")]
        assert "value_easy_privacy" == row[header_row.index("list_labels_easy_privacy")]
        assert "value_moaab" == row[header_row.index("list_labels_moaab")]

        # Test values of row 2
        packet = data["all_labels2"]
        row = csv_converter.get_row(packet, "all_labels2")
        print row
        assert 0 == row[header_row.index("predicted_general")]
        assert 1 == row[header_row.index("predicted_package_name")]
        assert 2 == row[header_row.index("list_labels_easy_list")]
        assert 3 == row[header_row.index("list_labels_easy_privacy")]
        assert 4 == row[header_row.index("list_labels_moaab")]
