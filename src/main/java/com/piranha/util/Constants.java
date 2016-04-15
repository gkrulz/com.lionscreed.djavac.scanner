package com.piranha.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Padmaka on 2/15/16.
 */
public class Constants {
    private static final Logger log = Logger.getLogger(Constants.class);
    public static String SOURCE_PATH;
    public static String DESTINATION_PATH;
    public static String PATH_SEPARATOR;

    static {
        String pathSeparator = null;

        if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux")) {
            pathSeparator = "/";
        } else if (System.getProperty("os.name").contains("Windows")) {
            pathSeparator = "\\";
        }

        PATH_SEPARATOR = pathSeparator;
        SOURCE_PATH = PiranhaConfig.getProperty("SOURCE_PATH");
        DESTINATION_PATH = PiranhaConfig.getProperty("DESTINATION_PATH");
    }
}
