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
