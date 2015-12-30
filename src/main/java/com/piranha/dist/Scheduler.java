package com.piranha.dist;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by Padmaka on 12/30/15.
 */
public class Scheduler {

    public void makeSchedule(ArrayList<JsonObject> classes, ArrayList<JsonObject> detailedClassList) {

        for (JsonObject classJson : classes) {
            JsonArray dependencies = classJson.get("dependencies").getAsJsonArray();

        }
    }
}
