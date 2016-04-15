package com.piranha.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by root on 4/13/16.
 */
public class PiranhaConfig {
    private static Properties properties = new Properties();

    public static void innitializeProperties() throws IOException {

//        properties.load(PiranhaConfig.class.getClassLoader().getResourceAsStream("config.properties"));
       properties.load(new FileReader(new File("/piranha/master/config.properties")));
    }

    public static String getProperty(String name) {
        return properties.getProperty(name);
    }
}
