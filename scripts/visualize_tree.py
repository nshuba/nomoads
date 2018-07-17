#!/usr/bin/python

"""
Use this script to visualize trees (convert from .dot file to a .png image)

USAGE:
./visualize_tree.py ~/dot.exe tree.dot
"""

import sys, os
import re

from subprocess import call


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print "ERROR: incorrect number of arguments. Correct usage:"
        print "\t$ ./visualize_tree.py PATH_TO_DOT PATH_TO_TREE_FILE"
        sys.exit(1)

    dotFile = sys.argv[2]
    if not dotFile.endswith(".dot"):
        print "ERROR: .dot file expected in the second argument"
        sys.exit(2)

    dir_path = os.path.dirname(dotFile)
    try:
        os.chdir(dir_path)
    except OSError as e:
        print "Could not change directory to " + dir_path
        print e
        sys.exit(3)

    # Read in the dot file
    lines = []
    with open(dotFile, 'r') as file:
        for line in file:
            nl = re.sub(r'label="0\s\(.+\)" shape=box style=filled',
                        'label="No Ad" shape=box style=filled color=black fillcolor=chartreuse3',
                        line)
            nl = re.sub(r'label="1\s\(.+\)" shape=box style=filled',
                        'label="Ad" shape=box style=filled color=black fillcolor=indianred1',
                        nl)

            # Use this code to fix unescaped quotes within trees:
            # arr = line.split("\"")
            # if len(arr) <= 3: # meaning only two quotation marks found
            #     nl = line # keep line as is
            # else:
            #     # only the first and last quotes need to be kept unescaped
            #     nl = arr[0] + "\"" + arr[1]
            #     i = 2
            #     while i < len(arr) - 1:
            #         nl += "\\\"" + arr[i]
            #         i += 1
            #     # only the first and last quotes need to be kept unescaped
            #     nl += "\"" + arr[i]
            #
            #     print "Changed " + line + "\t->\t" + nl

            lines.append(nl)


    # Update the file
    with open(dotFile, 'w') as file:
        for line in lines:
            file.write(line)

    # Execute command: dot.exe -o tree.png tree.dot -Tpng
    # Note: use -Tsvg to save very large trees in SVG format and view in your browser
    pngFile = re.sub("\.dot", ".png", dotFile)
    cmd = [sys.argv[1], "-o", pngFile, dotFile, "-Tpng"]
    call(cmd)

    # About the trees: https://weka.wikispaces.com/What+do+those+numbers+mean+in+a+J48+tree%3F
    # "The first number is the total number of instances (weight of instances) reaching the leaf.
    # The second number is the number (weight) of those instances that are misclassified."

    print "Picture generated: " + pngFile