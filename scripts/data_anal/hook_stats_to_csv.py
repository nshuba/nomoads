#!/usr/bin/python

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

def json_file_to_csv(full_path, csv_writer):
    with open(full_path, "r") as jf:
        data = json.load(jf)

        for key in data:
            packet = data[key]
            ats_pkg = packet.get(utils.json_key_ats_pkg, "app")
            row = [packet[utils.json_key_package_name], key, ats_pkg, packet[utils.json_key_type]]
            csv_writer.writerow(row)

if __name__ == '__main__':
    ap = argparse.ArgumentParser(
        description="Parses raw data and prints stats about hook usage")
    ap.add_argument('dir', type=utils.readable_dir,
                    help='Directory containing JSON files in NoMoAds JSON format.')
    ap.add_argument('csv_file',
                    help='CSV file to write to.')
    args = ap.parse_args()

    with open(args.csv_file, "wb") as f:
        header_row = [utils.json_key_package_name, utils.json_key_id, utils.json_key_ats_pkg,
                      utils.json_key_type]
        csv_writer = csv.writer(f)
        csv_writer.writerow(header_row)

        for fn in os.listdir(args.dir):
            full_path = args.dir + "/" + fn
            json_file_to_csv(full_path, csv_writer)
