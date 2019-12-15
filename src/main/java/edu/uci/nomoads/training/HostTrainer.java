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
 * Trains based on the fully qualified domain name
 */
public class HostTrainer extends DomainAdsTrainer {

    public HostTrainer(ServerUtils serverUtils) {
        super(serverUtils);
        jsonAttrKey = JsonKeyDef.F_KEY_HOST;
    }

    /**
     * Fetches the host from provided packet
     * @param packet packet to fetch the host from
     * @return hostname
     */
    @Override
    protected String getAttrFromPacket(JSONObject packet) {
        return super.getAttrFromPacket(packet);
    }

}
