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
package edu.uci.nomoads.prediction;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Interface for Deep Packet Inspection implementations
 */
public interface DPIInterface {

    /**
     * Initializes the DPI object with the provided strings that need to be searched for
     * @param searchStrings the strings this object should search for
     */
    void init(String[] searchStrings);

    /**
     * Perform DPI on the given packet, searching for strings that were passed in
     * previously in the init method
     * @param packet a {@link ByteBuffer} containing the packet
     * @param size size of the packet
     * @return a list of strings found. Each string is followed by the ending position
     * of where it was found in the packet.
     */
    ArrayList<String> search(ByteBuffer packet, int size);
}
