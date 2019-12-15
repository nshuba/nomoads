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

import json, csv
import argparse, sys, os

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + os.sep + "..")
from utils import utils
from utils import settings

key_tr_time = "traing_time"
key_tree_size = "numNonLeafNodes"

def process_file(logs_dir, fn):
    file_path = logs_dir + "/" + fn
    with open(file_path, "r+") as jf:
        data = json.load(jf)

        # Re-write data in a nicer format than what Java left it in
        jf.seek(0)
        jf.write(json.dumps(data, sort_keys=True, indent=4))
        jf.truncate()

        return data[key_tr_time], data[key_tree_size]

def process_logs(logs_dir):
    # Per-classifier stats (macro-stats):
    all_files_tr_time = []
    all_files_tree_size = []

    for fn in os.listdir(logs_dir):
        (tr_time, tree_size) = process_file(logs_dir, fn)

        all_files_tr_time.append(tr_time)
        all_files_tree_size.append(tree_size)

    print "max tree size = " + str(max(all_files_tree_size))
    print "\nMACRO RESULTS (Per-file):"
    utils.print_stats(key_tr_time, all_files_tr_time, is_percent=False)
    utils.print_stats(key_tree_size, all_files_tree_size, is_percent=False)

if __name__ == '__main__':
    desc = "Parses log files of full classifier builds to extract training time and tree sizes"
    ap = argparse.ArgumentParser(description=desc)
    ap.add_argument('config_file', help='The configuration file from the config dir to use')

    args = ap.parse_args()
    settings.init(args.config_file)

    print "Will evaluate: " + settings.LOGS_DIR
    process_logs(settings.LOGS_DIR)
