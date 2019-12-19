#!/usr/bin/python

"""
Parses LibRadar++ and LiteRadar tag rules to figure out number of A&T libs in each app
"""

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

import argparse
import os
import json, csv
import utils
from sets import Set

from extract_from_tshark import parse_literadar_csv, parse_literadar

json_key_ats_pkg = "ats_pkg"
json_key_ats = "ats"
json_key_lib = "lib"

if __name__ == '__main__':

    ap = argparse.ArgumentParser(description="Match packet data against a given filter list.")
    ap.add_argument('csv_tag_file', type=utils.readable_file,
                    help='CSV file containing LiteRadar tag rules.')
    ap.add_argument('dir', type=utils.readable_dir,
                    help='Directory containing Libradar++ output.')
    args = ap.parse_args()
    global_ats_pkgs = parse_literadar_csv(args.csv_tag_file)
    lib_stats = Set()

    apps = Set()
    for fn in os.listdir(args.dir):
        full_path = args.dir + "/" + fn
        print full_path
        ats_pkgs = parse_literadar(full_path, global_ats_pkgs)
        if len(ats_pkgs) == 0:
            print "*** NO ATS PKGS"
            apps.add(fn)

    print "Apps w/o ats: " + str(len(apps))
    print apps

    # TODO: find number of libraries that contain custom SSL. Notes:
    #     for pkg in ats_pkgs:
    #         lib_stats.add(pkg)
    #
    # print "Total packages: " + str(len(global_ats_pkgs))
    # print "ATS packages in our APKs: " + str(len(lib_stats))
    # print lib_stats

    #
    # {
    #     "containsSSL": false,
    #     "type": "lib",
    #     "trace": "java.lang.Exception\n\tat java.lang.System.loadLibrary(Native Method)\n\tat com.android.webview.chromium.WebViewChromiumFactoryProvider.initialize(WebViewChromiumFactoryProvider.java:239)\n\tat com.android.webview.chromium.WebViewChromiumFactoryProvider.<init>(WebViewChromiumFactoryProvider.java:195)\n\tat java.lang.reflect.Constructor.newInstance0(Native Method)\n\tat java.lang.reflect.Constructor.newInstance(Constructor.java:430)\n\tat android.webkit.WebViewFactory.getProvider(WebViewFactory.java:198)\n\tat android.webkit.WebView.getFactory(WebView.java:2325)\n\tat android.webkit.WebView.ensureProviderCreated(WebView.java:2320)\n\tat android.webkit.WebView.setOverScrollMode(WebView.java:2379)\n\tat android.view.View.<init>(View.java:4023)\n\tat android.view.View.<init>(View.java:4146)\n\tat android.view.ViewGroup.<init>(ViewGroup.java:579)\n\tat android.widget.AbsoluteLayout.<init>(AbsoluteLayout.java:55)\n\tat android.webkit.WebView.<init>(WebView.java:627)\n\tat android.webkit.WebView.<init>(WebView.java:572)\n\tat android.webkit.WebView.<init>(WebView.java:555)\n\tat com.nbc.nbcsports.authentication.tve.AuthenticationWebView.<init>(AuthenticationWebView.java:43)\n\tat com.nbc.nbcsports.authentication.tve.AuthenticationWebView.<init>(AuthenticationWebView.java:39)\n\tat java.lang.reflect.Constructor.newInstance0(Native Method)\n\tat java.lang.reflect.Constructor.newInstance(Constructor.java:430)\n\tat android.view.LayoutInflater.createView(LayoutInflater.java:645)\n\tat android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:787)\n\tat android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:727)\n\tat android.view.LayoutInflater.rInflate(LayoutInflater.java:858)\n\tat android.view.LayoutInflater.rInflateChildren(LayoutInflater.java:821)\n\tat android.view.LayoutInflater.inflate(LayoutInflater.java:518)\n\tat android.view.LayoutInflater.inflate(LayoutInflater.java:426)\n\tat android.view.LayoutInflater.inflate(LayoutInflater.java:377)\n\tat com.android.internal.policy.PhoneWindow.setContentView(PhoneWindow.java:412)\n\tat android.app.Activity.setContentView(Activity.java:2414)\n\tat com.nbc.nbcsports.ui.core.BaseActivity.setContentView(BaseActivity.java:48)\n\tat com.nbc.nbcsports.activities.MainActivity.onCreate(MainActivity.java:450)\n\tat android.app.Activity.performCreate(Activity.java:6679)\n\tat android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1118)\n\tat android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2618)\n\tat android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2726)\n\tat android.app.ActivityThread.-wrap12(ActivityThread.java)\n\tat android.app.ActivityThread$H.handleMessage(ActivityThread.java:1477)\n\tat android.os.Handler.dispatchMessage(Handler.java:102)\n\tat android.os.Looper.loop(Looper.java:154)\n\tat android.app.ActivityThread.main(ActivityThread.java:6119)\n\tat java.lang.reflect.Method.invoke(Native Method)\n\tat com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:886)\n\tat com.android.internal.os.ZygoteInit.main(ZygoteInit.java:776)\n",
    #     "lib": "libwebviewchromium_plat_support.so"
    # }
    #
    # with open(args.csv_file, "wb") as f:
    #     header_row = [utils.json_key_package_name, json_key_ats, json_key_ats_pkg, json_key_lib]
    #     csv_writer = csv.writer(f)
    #     csv_writer.writerow(header_row)
    #
    #     for fn in os.listdir(args.dir):
    #         full_path = args.dir + "/" + fn
    #         compare(full_path, csv_writer)
