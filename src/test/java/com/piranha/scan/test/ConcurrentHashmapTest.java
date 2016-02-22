package com.piranha.scan.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.util.Directory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Padmaka on 2/17/16.
 */
public class ConcurrentHashmapTest {

    public static void main(String[] args) {
        String className = "com.lionscreed.djavac.compiler";
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonObject classJson = new JsonObject();
        classJson.addProperty("className", "ClassName");
        JsonObject classJson2 = new JsonObject();
        classJson.addProperty("className", "A");

        Directory directory = new Directory();

        directory.put(className, classJson);
        directory.put("com.lionscreed.djavac.compilerA.A", classJson2);

        Object resultObj = directory.get("com.lionscreed.djavac");
        ConcurrentHashMap<String, Object> result = null;

        if (resultObj instanceof ConcurrentHashMap) {
            result = (ConcurrentHashMap<String, Object>) resultObj;
        }

        System.out.println(directory.find("com.lionscreed.djavac"));
    }
}
