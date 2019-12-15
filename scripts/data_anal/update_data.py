#!/usr/bin/python

"""
Updates JSON to store prediction values/labels in a more structured format. Prepares the data
for the `results_to_csv` script.
TODO: this should be done by code that generates these values to avoid this extra step
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

import argparse
import sys, os
import json, csv

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + os.sep + "..")
from utils import utils

json_key_predict = "predicted"
json_key_list_labels = "list_labels"

def update(full_path):
    with open(full_path, "r+") as jf:
        data = json.load(jf)

        new_data = {}
        for key in data:
            packet = data[key]

            packet[json_key_predict] = {}
            if "predicted_general" in packet:
                packet[json_key_predict]["general"] = packet["predicted_general"]
                del packet["predicted_general"]
            if "predicted_package_name" in packet:
                packet[json_key_predict]["package_name"] = packet["predicted_package_name"]
                del packet["predicted_package_name"]

            packet[json_key_list_labels] = {}
            packet[json_key_list_labels]["easy_list"] = packet["easy_list"]
            del packet["easy_list"]
            packet[json_key_list_labels]["easy_privacy"] = packet["easy_privacy"]
            del packet["easy_privacy"]
            packet[json_key_list_labels]["moaab"] = packet["moaab"]
            del packet["moaab"]

            new_data[key] = packet

        jf.seek(0)
        jf.write(json.dumps(new_data, sort_keys=True, indent=4))
        jf.truncate()


if __name__ == '__main__':
    ap = argparse.ArgumentParser(
        description="Parse stats about the provided packets and save them in CSV format.")
    ap.add_argument('dir', type=utils.readable_dir,
                    help='Directory containing JSON files in NoMoAds JSON format.')
    args = ap.parse_args()

    for fn in os.listdir(args.dir):
        full_path = args.dir + "/" + fn
        update(full_path)
