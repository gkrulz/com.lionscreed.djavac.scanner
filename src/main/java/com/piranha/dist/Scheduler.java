package com.piranha.dist;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by Padmaka on 12/30/15.
 */
public class Scheduler {
    private static final Logger log = Logger.getLogger(Scheduler.class);

    /***
     * The methos to make the schedule of compilation
     * @param classes
     * @param detailedClassList
     * @return the schedule
     */
    public ArrayList<ArrayList<JsonObject>> makeSchedule(ArrayList<JsonObject> classes, ArrayList<JsonObject> detailedClassList) {
        ArrayList<ArrayList<JsonObject>> schedule = new ArrayList<>();
        int noOfRounds = classes.size();

        for (int i = 0; i < noOfRounds; i++) {
            ArrayList<JsonObject> dependencyFreeClasses = new ArrayList<>();
            log.debug("Compilation round " + (i + 1));

            for (JsonObject classJson : classes) {
                JsonArray dependencies = classJson.get("dependencies").getAsJsonArray();
                String currentDependency = null;

                if (dependencies.size() == 0) {
                    dependencyFreeClasses.add(classJson);
                    log.debug(classJson.get("absoluteClassName"));
                }
            }

            for (JsonObject dependency : dependencyFreeClasses) {
                classes.remove(dependency);
            }

            for (int j = 0; j < classes.size(); j++) {
                JsonArray newDependencyArray = new JsonArray();

                ArrayList<JsonObject> newClasses = classes;

                for (int k = 0; k < newClasses.get(j).get("dependencies").getAsJsonArray().size(); k++) {

                    if (newClasses.get(j).get("dependencies").getAsJsonArray().contains(newClasses.get(j).get("dependencies").getAsJsonArray().get(k))) {
                        classes.get(j).get("dependencies").getAsJsonArray().remove(newClasses.get(j).get("dependencies").getAsJsonArray().get(k));
                    }
                }


            }
            schedule.add(dependencyFreeClasses);
            log.debug(classes.size());
            log.debug(classes);

            if (classes.size() == 0){
                break;
            }
        }
        log.debug(schedule);
        return schedule;
    }
}
