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

import os, sys
from adblockparser import AdblockRules
import argparse
import json
import re

from urlparse import urlsplit

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + os.sep + "..")
from utils import utils, json_keys

key_req_with = "x-requested-with"
key_xml_http_req = "xmlhttprequest"

key_http = "http"
key_https = "https"

type_other = 'other'
type_script = 'script'
type_stylesheet = 'stylesheet'
type_image = 'image'
type_subdocument = 'subdocument'

# Patterns for matching URL to find content type (based on AdblockPlus for Android)
# Note: match these against the URL path only, without including URL query and fragment
re_js = re.compile("\.js$", re.IGNORECASE)
re_css = re.compile("\.css$", re.IGNORECASE)
re_image = re.compile("\.(?:gif|png|jpe?g|bmp|ico)$", re.IGNORECASE)
re_html = re.compile("\.html?$", re.IGNORECASE)

# The Android code also includes fonts, but based on https://adblockplus.org/en/filters#options
# this is not a valid option in current ABP
#re_font = re.compile("\.(?:ttf|woff)$", re.IGNORECASE)

def init_rule_checker(filter_list_file):
    """
    Initializer an AdblockRules instance to correspond to a filter list stored in a given file.
    :param filter_list_file: The path to the filter list file.
    :return: An AdblockRules instance initialized to perform matching against the given filter list.
    """
    with open(filter_list_file, "r") as f:
        lines = f.readlines()
        return AdblockRules(lines)

def get_content_type(url_parsed):
    """
    Detects content type for the given URL.
    Based on:
    https://github.com/adblockplus/libadblockplus-android/blob/
        bcb385e81d4ce2ead519b662997e0865b79b4fb0/libadblockplus-android-webview/
        src/org/adblockplus/libadblockplus/android/webview/AdblockWebView.java

    :param url_parsed: The parsed URL, as returned by urlsplit
    :return: Content type as a string object that can be used in AdblockRules options
    """
    # TODO: use Content Type header as here: https://github.com/adblockplus/adblockplusandroid/blob/c629500610f55b0f1f84a851419271629a2f58c7/src/org/adblockplus/android/AdblockPlus.java
    url_path = url_parsed.path
    type = type_other
    if re_js.search(url_path):
        type = type_script
    elif re_css.search(url_path):
        type = type_stylesheet
    elif re_image.search(url_path):
        type = type_image
    elif re_html.search(url_path):
        type = type_subdocument

    return type

def get_origin(url_parsed):
    """
    Returns the origin of the provided URL.
    Based on: https://developer.mozilla.org/docs/Glossary/Origin

    :param url_parsed: The parsed URL, as returned by urlsplit
    :return: the origin of the URL
    """

    # Origin is defined by schema, host, and port.
    # Note that the resulting origin is not of valid browsing format, but this is not an issue here.
    return url_parsed.scheme + url_parsed.netloc


def isxmlreq_isthirdparty(url_parsed, httpheaders):
    """
    Parses the HTTP headers to find if the request is an xml request and if it's 3rd party

    :param url_parsed: The parsed URL, as returned by urlsplit
    :param httpheaders: HTTP headers in dictionary format, where each key is the HTTP header key,
        and each value is the HTTP header value
    :return: a tuple containing two booleans: (is_xml_request, is_third_party)
    """
    # TODO: isthirdparty is done differently by ABP: https://github.com/adblockplus/libadblockplus/blob/786e16cb499ccd513ace6216737a4b262c007b04/lib/basedomain.js
    is_third_party = False
    is_xml_request = False
    for key in httpheaders:
        lower_key = key.lower()
        if lower_key == json_keys.referer:
            if get_origin(urlsplit(httpheaders[key])) != get_origin(url_parsed):
                is_third_party = True
        elif lower_key == key_req_with:
            if httpheaders[key].lower() == key_xml_http_req:
                is_xml_request = True

    return (is_xml_request, is_third_party)

def get_options(url, httpheaders):
    """
    Returns the AbblockPlus options to be set for the provided URL and HTTP headers

    :param url: The full URL including query and fragment
    :param httpheaders: HTTP headers in dictionary format, where each key is the HTTP header key,
        and each value is the HTTP header value
    :return: a dictionary of options that can be passed to rules.should_block
    """

    url_parsed = urlsplit(url)
    content_type = get_content_type(url_parsed)
    (is_xml_request, is_third_party) = isxmlreq_isthirdparty(url_parsed, httpheaders)

    return {content_type: True, 'xmlhttprequest': is_xml_request, 'third-party': is_third_party}

def get_block_decision(ruleset, pkt_nomoads_json):
    """
    Given a single packet in NoMoAds JSON format, return if the given filter list blocks that packet.
    :param ruleset: an AdblockRules instance that has been initialized with a given set of rules.
    :param pkt_nomoads_json: A single packet in NoMoAds JSON format.
    :return: True if the ruleset would block the packet, False otherwise.
    """

    # We always return False if there is no "host" in the packet
    if json_keys.host not in pkt_nomoads_json:
        return False

    port = pkt_nomoads_json[json_keys.dst_port]

    # Note: usually we only deal with HTTP/S:
    if port == 443:
        url = "https://"
    else:
        url = "http://"

    url += pkt_nomoads_json[json_keys.host] + pkt_nomoads_json.get(json_keys.uri, "")
    headers = pkt_nomoads_json.get(json_keys.headers, {})
    options = get_options(url, headers)
    #print options

    return ruleset.should_block(url, options)


def annotate_nomoads_json(ruleset, nomoads_json_file, filter_list_name=None):
    """
    Open a JSON file that contains packets in NoMoAds format, and annotate each packet with the given AdblockRules'
    blocking decision, and (optionally) a name for the filter list backing the AdblockRules instance.
    :param ruleset: An AdblockRules instance that determines if each individual packet should be blocked or not.
    :param nomoads_json_file: A JSON file with packets in NoMoAds format.
    :param filter_list_name: A name to associate with the ruleset.
    :return: The original JSON, annotated with blocking decision and filter list name.
    """
    with open(nomoads_json_file, "r") as jf:
        decoder = json.JSONDecoder()
        annotated = {}
        root_obj = decoder.decode(jf.read())
        for key in root_obj:
            print "\t" + str(key)
            pkt = root_obj[key]
            blocked = get_block_decision(ruleset, pkt)
            pkt[filter_list_name] = 1 if blocked else 0
            annotated[key] = pkt
        return annotated


def write_annotated_nomoads_json(data, file_out):
    """
    Write annotated NoMoAds JSON to a file.
    :param data: The annotated NoMoAds JSON.
    :param file_out: The file to output the annotated NoMoAds JSON to.
    """
    with open(file_out, "w") as jf:
        jf.seek(0)
        jf.write(json.dumps(data, sort_keys=True, indent=4))
        jf.truncate()

if __name__ == '__main__':
    ap = argparse.ArgumentParser(description="Match packet data against a given filter list.")
    ap.add_argument('dir', type=utils.readable_dir,
                    help='Directory containing JSON files in NoMoAds JSON format.')
    ap.add_argument('filter_list', type=utils.readable_file,
                    help='Path to a filter list in EasyList format.')
    ap.add_argument('out_dir', type=utils.readable_dir,
                    help='Path to directory where results are to be written.')
    ap.add_argument('--filter_list_name', default="filter_list",
                    help='A name that identifies filter_list. Packets in out_dir will be annotated '
                         'with this name.')
    args = ap.parse_args()
    
    print "Initializing rules..."
    rule_checker = init_rule_checker(args.filter_list)
    print "Rules ready!"
    
    for fn in os.listdir(args.dir):
        print fn
        full_path = args.dir + "/" + fn

        annotated_json = annotate_nomoads_json(rule_checker, full_path, args.filter_list_name)
        write_annotated_nomoads_json(annotated_json, args.out_dir + "/" + fn)
