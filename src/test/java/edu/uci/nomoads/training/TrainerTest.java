/*
 * This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
 * Copyright (C) 2018, 2019 Anastasia Shuba
 *
 * NoMoAds is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NoMoAds is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.uci.nomoads.training;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Nastia on 7/28/2019.
 */
public class TrainerTest {
    private DataSplitter dataSplitter;
    private JSONParser parser;

    @Before
    public void setUp() {
        Config config = new Config("config.cfg");
        parser = new JSONParser();
        dataSplitter = new DataSplitter(config);
    }

    /**
     * Tests a scenario where we have an even number of both positive and negative samples
     * @throws Exception
     */
    @Test
    public void testSplitEven() throws Exception {
        Info inf = new Info();
        inf.initNumPos = 10;
        inf.initNumNeg = 10;
        inf.initNumTotal = 20;

        JSONObject trFlows = (JSONObject) parser.parse(
                new FileReader("src/test/test_even.json"));
        ArrayList<Set<String>> bins = dataSplitter.splitData(inf, trFlows);

        assertEquals(DataSplitter.NUM_CROSS_FOLDS, bins.size());
        for (Set<String> bin : bins) {
            assertEquals(inf.initNumTotal/DataSplitter.NUM_CROSS_FOLDS, bin.size());

            testSingleBin(bin, trFlows, inf.initNumPos/DataSplitter.NUM_CROSS_FOLDS,
                    inf.initNumNeg/DataSplitter.NUM_CROSS_FOLDS);
        }

        testAllAdded(inf.initNumTotal, bins);
    }

    /**
     * Tests a scenario where we have an uneven number of samples
     * @throws Exception
     */
    @Test
    public void testSplitUneven() throws Exception {
        Info inf = new Info();
        inf.initNumPos = 26;
        inf.initNumNeg = 12;
        inf.initNumTotal = 38;

        JSONObject trFlows = (JSONObject) parser.parse(
                new FileReader("src/test/test_uneven.json"));
        ArrayList<Set<String>> bins = dataSplitter.splitData(inf, trFlows);


        assertEquals(DataSplitter.NUM_CROSS_FOLDS, bins.size());

        int minBinSize = inf.initNumTotal/DataSplitter.NUM_CROSS_FOLDS;
        int minPos = inf.initNumPos/DataSplitter.NUM_CROSS_FOLDS;
        int minNeg = inf.initNumNeg/DataSplitter.NUM_CROSS_FOLDS;

        Set<String> bin0 = bins.get(0);
        // This bin should have an extra positive sample and an extra negative sample
        assertEquals(minBinSize + 2, bin0.size());
        testSingleBin(bin0, trFlows, minPos + 1, minNeg + 1);

        Set<String> bin1 = bins.get(1);
        // This bin should have an extra negative sample
        assertEquals(minBinSize + 1, bin1.size());
        testSingleBin(bin1, trFlows, minPos, minNeg + 1);

        // The rest of the bins should have no extra samples
        for (int i = 2; i < bins.size(); i++) {
            Set<String> bin = bins.get(i);
            assertEquals(minBinSize, bin.size());
            testSingleBin(bin, trFlows, minPos, minNeg);
        }

        testAllAdded(inf.initNumTotal, bins);
    }

    /**
     * Makes sure all IDs were selected exactly once
     * @param total - total number of data points considered
     * @param bins - the resultant bins
     */
    private void testAllAdded(int total, ArrayList<Set<String>> bins) {
        Set<String> selectedIDs = new HashSet<>(total);
        for (Set<String> bin : bins)
            selectedIDs.addAll(bin);
        assertEquals(total, selectedIDs.size());
    }

    /**
     * Checks the number of positive and negative samples in the provided bin
     * @param bin the bin to test
     * @param trFlows training data
     * @param expPos expected number of positive samples
     * @param expNeg expected number of negative samples
     */
    private void testSingleBin(Set<String> bin, JSONObject trFlows, int expPos, int expNeg) {
        int numPos = 0;
        int numNeg = 0;
        for(String s : bin) {
            if (ServerUtils.getIntFromJSONObject((JSONObject) trFlows.get(s),
                    dataSplitter.trainer.jsonKeyLabel) == Trainer.LABEL_POSITIVE)
                numPos++;
            else
                numNeg++;
        }

        assertEquals(expPos, numPos);
        assertEquals(expNeg, numNeg);
    }

}