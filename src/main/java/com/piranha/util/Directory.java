package com.piranha.util;

import com.google.gson.JsonObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Padmaka on 2/17/16.
 */
public class Directory {

    ConcurrentHashMap<String, Object> root = new ConcurrentHashMap<>();


    public void put(String key, JsonObject value) {

        String[] keyParts = key.split("\\.");
        ConcurrentHashMap<String, Object> currentLevel = root;

        for (int x = 0; x < keyParts.length; x++) {

            if (x == (keyParts.length-1)) {
                currentLevel.put(keyParts[x],value);
            } else {

                if (currentLevel.get(keyParts[x]) == null) {
                    currentLevel.put(keyParts[x], new ConcurrentHashMap<>());
                }
                currentLevel = (ConcurrentHashMap<String,Object>)currentLevel.get(keyParts[x]);

            }

        }
    }

    public Object get(String key) {

        String[] keyParts = key.split("\\.");
        ConcurrentHashMap<String, Object> currentLevel = root;

        for (int i = 0; i < keyParts.length; i++) {

            if (i == keyParts.length - 1 && currentLevel.get(keyParts[i]) != null) {
                return currentLevel.get(keyParts[i]);
            } else if (currentLevel.get(keyParts[i]) != null) {
                try {
                    currentLevel = (ConcurrentHashMap<String, Object>) currentLevel.get(keyParts[i]);
                } catch (ClassCastException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return null;
    }

    public JsonObject find(String key) {

        String[] keyParts = key.split("\\.");
        ConcurrentHashMap<String, Object> currentLevel = root;

        for (int i = 0; i < keyParts.length; i++) {

            if (i == keyParts.length - 1 && currentLevel.get(keyParts[i]) != null &&
                    currentLevel.get(keyParts[i]) instanceof JsonObject) {
                return (JsonObject) currentLevel.get(keyParts[i]);
            } else if (currentLevel.get(keyParts[i]) != null && currentLevel.get(keyParts[i]) instanceof JsonObject) {
                return (JsonObject) currentLevel.get(keyParts[i]);
            } else if (currentLevel.get(keyParts[i]) != null && currentLevel.get(keyParts[i]) instanceof ConcurrentHashMap){
                try {
                    currentLevel = (ConcurrentHashMap<String, Object>) currentLevel.get(keyParts[i]);
                } catch (ClassCastException e) {
                    return null;
                }
            } else {
                return null;
            }

        }

        return null;
    }
}
