package com.piranha.dist;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by Padmaka on 12/30/15.
 */
public class Scheduler {
    private static final Logger log = Logger.getLogger(Scheduler.class);

    public void makeSchedule(ArrayList<JsonObject> classes, ArrayList<JsonObject> detailedClassList) {
        ArrayList<JsonObject> dependencyFreeClasses = new ArrayList<>();
        log.debug("Files to be compiled first");

        for (JsonObject classJson : classes) {
            JsonArray dependencies = classJson.get("dependencies").getAsJsonArray();

            if (dependencies.size() == 0) {
                log.debug(classJson.get("className"));
            }
        }
    }
}
