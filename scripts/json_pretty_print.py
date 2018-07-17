#!/usr/bin/python

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