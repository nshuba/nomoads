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
