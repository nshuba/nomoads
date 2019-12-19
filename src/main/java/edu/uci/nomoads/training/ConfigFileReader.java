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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class for reading and processing configuration files
 */
abstract class ConfigFileReader {

    public void readFile(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();
            while (line != null) {
                // Skip comments and empty spaces
                if (line.startsWith("#") || line.trim().equals("")) {
                    line = br.readLine();
                    continue;
                }
                processLine(line);
                line = br.readLine();
            }
            onProcessingDone();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Called when we are done reading the file */
    public void onProcessingDone() {};

    public abstract void processLine(String line);
}
