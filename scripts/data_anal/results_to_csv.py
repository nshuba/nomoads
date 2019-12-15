#!/usr/bin/python

"""
Parse stats about the provided packets and save them in CSV format
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
from utils import utils, json_keys
from utils import settings

class CSVconverter(object):

    def __init__(self, sample_packet):
        self.predict_labels = []
        self.list_labels = []
        self.header_row = [json_keys.package_name,
                           json_keys.ats_pkg,
                           json_keys.id,
                           json_keys.host,
                           json_keys.domain,
                           json_keys.uri,
                           json_keys.referer,
                           json_keys.headers,
                           json_keys.method,
                           settings.json_key_label,
                           json_keys.pii_label]

        if json_keys.predicted in sample_packet:
            for predict_label in sample_packet[json_keys.predicted]:
                self.header_row.append(json_keys.predicted + "_" + predict_label)
                self.predict_labels.append(predict_label)

        if json_keys.list_label in sample_packet:
            for list_label in sample_packet[json_keys.list_label]:
                self.header_row.append(json_keys.list_label + "_" + list_label)
                self.list_labels.append(list_label)

    def get_row(self, packet, key):
        ats_pkg = packet.get(json_keys.ats_pkg, "app")
        referer = ""
        hdrs = ""
        if json_keys.headers in packet:
            hdrs = packet[json_keys.headers]
            referer = hdrs.get(json_keys.referer, referer)

        row = [packet[json_keys.package_name],
               ats_pkg,
               key,
               packet[json_keys.host],
               packet[json_keys.domain],
               packet[json_keys.uri],
               referer,
               hdrs,
               packet[json_keys.method],
               packet[settings.json_key_label],
               packet[json_keys.pii_label]]

        for predict_label in self.predict_labels:
            row.append(packet[json_keys.predicted][predict_label])

        for list_label in self.list_labels:
            row.append(packet[json_keys.list_label][list_label])

        return row

    def get_header_row(self):
        return self.header_row

if __name__ == '__main__':
    ap = argparse.ArgumentParser(
        description="Parse stats about the provided packets and save them in CSV format.")
    ap.add_argument('config', help='Configuration file.')
    ap.add_argument('dir', type=utils.readable_dir,
                    help='Directory containing JSON files in NoMoAds JSON format.')
    ap.add_argument('csv_file', help='CSV file to write to.')
    args = ap.parse_args()

    settings.init(args.config)

    # Get a sample entry
    files_list = os.listdir(args.dir)
    with open(args.dir + "/" + files_list[0], "r") as jf:
        data = json.load(jf)
    first_entry = data[data.keys()[0]]
    csv_converter = CSVconverter(first_entry)

    with open(args.csv_file, "wb") as f:
        csv_writer = csv.writer(f)
        csv_writer.writerow(csv_converter.get_header_row())

        for fn in files_list:
            full_path = args.dir + "/" + fn

            with open(full_path, "r") as jf:
                data = json.load(jf)
                for key in data:
                    packet = data[key]
                    csv_writer.writerow(csv_converter.get_row(packet, key))
