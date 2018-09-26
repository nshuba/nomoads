/*
 *  This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
 *  Copyright (C) 2018 Anastasia Shuba, University of California, Irvine.
 *
 *  NoMoAds is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  NoMoAds is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.nomoads.training;

import org.json.simple.JSONObject;

/**
 * Trains on URLs
 */
class UrlAdsTrainer extends UrlPathAdsTrainer {

    public UrlAdsTrainer(ServerUtils serverUtils) {
        super(serverUtils);
    }

    public String getLine(JSONObject packet) {
        String line = super.getLine(packet); // gets URI
        line += "host: " + packet.get(JsonKeyDef.F_KEY_HOST) + "\r\n";

        return line;
    }
}
