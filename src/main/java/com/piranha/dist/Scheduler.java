package com.piranha.dist;

import com.google.gson.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by Padmaka on 12/30/15.
 */
public class Scheduler {
    private static final Logger log = Logger.getLogger(Scheduler.class);

    /***
     * The method to make the schedule of compilation
     *
     * @param classes list of json objects containing all classes
     * @return the compilation schedule
     */
    public ArrayList<ArrayList<JsonObject>> makeSchedule(ArrayList<JsonObject> classes) {
        ArrayList<ArrayList<JsonObject>> schedule = new ArrayList<>();
        ArrayList<String> compiledClasses = new ArrayList<>();

        for (int i = 0; i < classes.size(); i++) {
            log.debug("Compilation Round: " + (i + 1));
            ArrayList<JsonObject> dependencyFreeClasses = new ArrayList<>();

            for (JsonObject classJson : classes) {
                boolean isDependencyFree = false;

                if (classJson.get("dependencies").getAsJsonArray().size() == 0 &&
                        !compiledClasses.contains(classJson.get("absoluteClassName").getAsString())) {

                    isDependencyFree = true;

                } else if (schedule.size() > 0) {
                    for (JsonElement dependency : classJson.get("dependencies").getAsJsonArray()) {

                        if (compiledClasses.contains(dependency.getAsString())) {
                            isDependencyFree = true;
                        }else {
                            isDependencyFree = false;
                        }


                        if (!isDependencyFree) {
                            break;
                        }
                    }

                    if (compiledClasses.contains(classJson.get("absoluteClassName").getAsString())) {
                        isDependencyFree = false;
                    }
                }

                if (isDependencyFree) {
                    dependencyFreeClasses.add(classJson);
                    log.debug(classJson.get("absoluteClassName").getAsString());
                }
            }

            if (!dependencyFreeClasses.isEmpty()) {
                schedule.add(dependencyFreeClasses);
            } else {
                break;
            }

            for (JsonObject classObj : dependencyFreeClasses) {
                compiledClasses.add(classObj.get("absoluteClassName").getAsString());
            }
        }

        return schedule;
    }
}
