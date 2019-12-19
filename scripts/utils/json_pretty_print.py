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

import json
import sys, os

def prettify_dir(dir):
    for fn in os.listdir(dir):
        full_path = dir + "/" + fn
        if os.path.isdir(full_path):
            prettify_dir(full_path)
            continue

        prettify_file(full_path)

def prettify_file(full_path):
    print full_path
    with open(full_path, "r+") as jf:
        data = json.load(jf)
        jf.seek(0)
        jf.write(json.dumps(data, sort_keys=True, indent=4))
        jf.truncate()

if len(sys.argv) < 2:
    print "Please provide file name!"
    sys.exit()

file = sys.argv[1]

if os.path.isdir(file):
    prettify_dir(file)
else:
    prettify_file(file)