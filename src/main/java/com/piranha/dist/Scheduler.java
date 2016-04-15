package com.piranha.dist;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
//    public ArrayList<ArrayList<JsonObject>> makeSchedule(ConcurrentHashMap<String, JsonObject> classes) {
//        ArrayList<ArrayList<JsonObject>> schedule = new ArrayList<>();
//        ArrayList<String> compiledClasses = new ArrayList<>();
//
//        for (int i = 0; i < classes.size(); i++) {
//            log.debug("Compilation Round: " + (i + 1));
//            ArrayList<JsonObject> dependencyFreeClasses = new ArrayList<>();
//
//            for (JsonObject classJson : classes.values()) {
//                boolean isDependencyFree = false;
//
//                if (classJson.get("dependencies").getAsJsonArray().size() == 0 &&
//                        !compiledClasses.contains(classJson.get("absoluteClassName").getAsString())) {
//
//                    isDependencyFree = true;
//
//                } else if (schedule.size() > 0) {
//                    for (JsonElement dependency : classJson.get("dependencies").getAsJsonArray()) {
//
//                        if (compiledClasses.contains(dependency.getAsString())) {
//                            isDependencyFree = true;
//                        } else {
//                            isDependencyFree = false;
//                        }
//
//
//                        if (!isDependencyFree) {
//                            break;
//                        }
//                    }
//
//                    if (compiledClasses.contains(classJson.get("absoluteClassName").getAsString())) {
//                        isDependencyFree = false;
//                    }
//                }
//
//                if (isDependencyFree) {
//                    dependencyFreeClasses.add(classJson);
//                    log.debug(classJson.get("absoluteClassName").getAsString());
//                }
//            }
//
//            if (!dependencyFreeClasses.isEmpty()) {
//                schedule.add(dependencyFreeClasses);
//            } else {
//                break;
//            }
//
//            for (JsonObject classObj : dependencyFreeClasses) {
//                compiledClasses.add(classObj.get("absoluteClassName").getAsString());
//            }
//        }
//
//        return schedule;
//    }

    public List<List<String>> findGraphDeadlock(ConcurrentHashMap<String, JsonObject> classes) {
        Gson gson = new Gson();
        Type mapType = new TypeToken<ConcurrentHashMap<String, String>>() {
        }.getType();

        DirectedGraph<String, DefaultEdge> dependancyGraph =
                new DefaultDirectedGraph<>(DefaultEdge.class);

        for (String key : classes.keySet()) {
            dependancyGraph.addVertex(key);
        }

        for (String key : classes.keySet()) {
            ConcurrentHashMap<String, String> dependencies = gson.fromJson(classes.get(key).get("dependencies").getAsString(), mapType);

            for (String dependency : dependencies.values()) {
                dependancyGraph.addEdge(dependency, key);
            }
        }

        SzwarcfiterLauerSimpleCycles<String, DefaultEdge> loopDetector = new SzwarcfiterLauerSimpleCycles<>(dependancyGraph);
        List<List<String>> loops = loopDetector.findSimpleCycles();

        return loops;
    }

    public ArrayList<ArrayList<JsonObject>> makeScheduleTemp(ConcurrentHashMap<String, JsonObject> classes) {
        ArrayList<ArrayList<JsonObject>> schedule = new ArrayList<>();
        HashMap<String, String> compiledClasses = new HashMap<>();
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        Type mapType = new TypeToken<ConcurrentHashMap<String, String>>() {}.getType();
        Gson gson = new Gson();

        List<List<String>> deadlocks = this.findGraphDeadlock(classes);

        //------------------------------------------------------------------------------------------------------
        log.debug(gson.toJson(deadlocks));

        HashMap<String, String> deadlockMap = new HashMap<>();

        HashSet<List<String>> newLists = new HashSet<>();

        for (List<String> set : deadlocks){
            for (String element : set){
                deadlockMap.put(element, element);
            }
        }

        for(List<String> x : deadlocks){
            List<String> currentList = x;
            for(List<String> currentList2 : deadlocks){
                if(x.equals(currentList2)){
                    continue;
                }
                if(currentList2.containsAll(currentList)){
                    currentList = currentList2;
                }
            }
            newLists.add(currentList);
        }

        log.debug(gson.toJson(newLists));

        List<HashSet<String>> newDeadlocks = new ArrayList<>();
        ArrayList<List<String>> usedLists = new ArrayList<>();

        for (List<String> subList : newLists){
            HashSet<String> newSubList = new HashSet<>();
            newSubList.addAll(subList);
            for (List<String> subSubList : newLists){
                if (subSubList.equals(subList)){
                    continue;
                }

                if (usedLists.contains(subSubList)){
                    continue;
                }

                for (String element : subSubList){
                    if (newSubList.contains(element)){
                        newSubList.addAll(subSubList);
                        usedLists.add(subSubList);
                        break;
                    }
                }
            }
            if (!usedLists.contains(subList)){
                newDeadlocks.add(newSubList);
            }
        }

        log.debug(gson.toJson(newDeadlocks));

        classes = this.prepareClasses(classes, newDeadlocks);
        //------------------------------------------------------------------------------------------------------

        ArrayList<JsonObject> round = null;

        for (int i = 0; i < classes.values().size(); i++) {

            round = new ArrayList<>();

            for (JsonObject classJson : classes.values()) {

                String className = classJson.get("absoluteClassName").getAsString();
                ConcurrentHashMap<String, String> dependencies = gson.fromJson(classJson.get("dependencies").getAsString(), mapType);
                boolean isDependencyFree = true;

                for (String dependency : dependencies.values()) {
                    if (compiledClasses.get(dependency) == null && deadlockMap.get(dependency) == null) {
                        isDependencyFree = false;
                        break;
                    }
                }

                if (compiledClasses.get(className) == null && isDependencyFree) {
                    round.add(classJson);
                }

            }

            if (round.size() > 0) {
                schedule.add(round);
                for (JsonObject classJson : round) {
                    String className = classJson.get("absoluteClassName").getAsString();
                    compiledClasses.put(className, className);
                }
            }
        }

        for (int x = 0; x < schedule.size(); x++) {
            log.debug(schedule.get(x).size());
            for (JsonObject classJson : schedule.get(x)) {
                String className = classJson.get("absoluteClassName").getAsString();
                log.debug(className);
            }
        }

        return schedule;
    }

    public ConcurrentHashMap<String, JsonObject> prepareClasses(ConcurrentHashMap<String, JsonObject> classes, List<HashSet<String>> newDeadlocks){
        ConcurrentHashMap<String, JsonObject> newClasses = new ConcurrentHashMap<>();
        HashMap<String, String> usedClasses = new HashMap<>();
        Gson gson = new Gson();
        Type mapType = new TypeToken<ConcurrentHashMap<String, String>>() {}.getType();
        JsonParser parser = new JsonParser();

        for (HashSet<String> deadlock : newDeadlocks){
            ArrayList<String> deadlockList = new ArrayList<>(deadlock);
            String className = deadlockList.get(0);
            newClasses.put(className, classes.get(className));
            deadlockList.remove(0);
            usedClasses.put(className, className);
            JsonObject classJson = newClasses.get(className);
            JsonArray toBeCompiledWith = new JsonArray();

            for (String name : deadlockList){
                toBeCompiledWith.add(classes.get(name));
                usedClasses.put(classes.get(name).get("absoluteClassName").getAsString(), classes.get(name).get("absoluteClassName").getAsString());
            }
            classJson.add("toBeCompiledWith", toBeCompiledWith);
        }

        for (String className : classes.keySet()) {
            if (!usedClasses.values().contains(className)){
                newClasses.put(className, classes.get(className));
            }
        }

        int count = 0;
        for (JsonObject classJson : newClasses.values()) {
            if (classJson.get("toBeCompiledWith") != null) {
                JsonArray toBeCompiledWith = classJson.get("toBeCompiledWith").getAsJsonArray();

                count = count + toBeCompiledWith.size() + 1;
            } else {
                count = count + 1;
            }
        }

        for (HashSet<String> deadlock : newDeadlocks) {
            ArrayList<String> deadlockList = new ArrayList<>(deadlock);
            String dependencyName = deadlockList.get(0);
            deadlockList.remove(0);

            for (JsonObject classJson : newClasses.values()){
                ConcurrentHashMap<String, String> dependencies = gson.fromJson(classJson.get("dependencies").getAsString(), mapType);

                for (String checkingName : deadlockList){
                    if (dependencies.get(checkingName) != null){
//                        dependencies.remove(checkingName);
                        dependencies.put(dependencyName, dependencyName);
                        classJson.addProperty("dependencies", gson.toJson(dependencies));
                    }
                }
            }
        }

        for (JsonObject classJson : newClasses.values()){
            if (classJson.get("toBeCompiledWith") != null) {
                JsonArray toBeCompiledWith = classJson.get("toBeCompiledWith").getAsJsonArray();
                ConcurrentHashMap<String, String> dependencies = gson.fromJson(classJson.get("dependencies").getAsString(), mapType);

                for (JsonElement element : toBeCompiledWith) {
                    JsonObject classObj = element.getAsJsonObject();
                    ConcurrentHashMap<String, String> subDependencies = gson.fromJson(classObj.get("dependencies").getAsString(), mapType);

                    dependencies.putAll(subDependencies);
                }

                dependencies.remove(classJson.get("absoluteClassName").getAsString());

                for (JsonElement element : toBeCompiledWith) {
                    JsonObject classObj = element.getAsJsonObject();
                    String className = classObj.get("absoluteClassName").getAsString();

                    dependencies.remove(className);
                }

                classJson.addProperty("dependencies", gson.toJson(dependencies));
            }
        }

        //TODO remove this part and fix the origin of the problem.
        for (JsonObject classJson : newClasses.values()) {
            if (classJson.get("toBeCompiledWith") != null) {
                JsonArray toBeCompiledWith = classJson.get("toBeCompiledWith").getAsJsonArray();
                String className = classJson.get("absoluteClassName").getAsString();
                ConcurrentHashMap<String, String> dependencies = gson.fromJson(classJson.get("dependencies").getAsString(), mapType);

                for (String dependency : dependencies.values()){
                    JsonObject classOjb = newClasses.get(dependency);
                    if (classOjb == null){
                        log.debug(dependency);
                        dependencies.remove(dependency);
                        classJson.addProperty("dependencies", gson.toJson(dependencies));
                    }else {
                        ConcurrentHashMap<String, String> subDependencies = gson.fromJson(classOjb.get("dependencies").getAsString(), mapType);

                        for (String d : subDependencies.values()) {
                            if (d.equals(className)) {
                                log.debug("dependency deadlock again!");
                            }
                        }
                    }
                }
            }
        }

        log.debug(count);
        return newClasses;
    }

//    public ConcurrentHashMap<String, JsonObject> removeDependencyDeadlocks(ConcurrentHashMap<String, JsonObject> classes) {
//
//        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
//        Type mapType = new TypeToken<ConcurrentHashMap<String, String>>() {}.getType();
//        JsonParser parser = new JsonParser();
//        Gson gson = new Gson();
//
//        for (JsonObject classJson : classes.values()) {
//            ConcurrentHashMap<String, String> dependencies = gson.fromJson(classJson.get("dependencies").getAsString(), mapType);
//            String className = classJson.get("absoluteClassName").getAsString();
//
//            for (String dependency : dependencies.values()) {
//
//                if (classes.get(dependency) != null) {
//                    JsonObject dependencyClassJson = classes.get(dependency).getAsJsonObject();
//                    String dependencyClassName = dependencyClassJson.get("absoluteClassName").getAsString();
//                    ConcurrentHashMap<String, String> dependencyList = gson.fromJson(dependencyClassJson.get("dependencies").getAsString(), mapType);
//
//                    if (dependencyList.contains(className)) {
//                        log.debug(className + " - dependency - " + dependencyClassName);
//                        classJson.add("toBeCompiledWith", dependencyClassJson);
//                        dependencies.putAll(dependencyList);
//
//                        HashSet<String> dependencyDependencySet = new HashSet<>();
//                        for (String dependenyDependency : dependencyList.values()) {
//                            dependencyDependencySet.add(dependenyDependency);
//                        }
//
//                        dependencyDependencySet.remove(className);
//                        JsonArray dependencyDependencies = parser.parse(gson.toJson(dependencyDependencySet)).getAsJsonArray();
//                        dependencyClassJson.add("dependencies", dependencyDependencies);
//
//                        HashMap<String, String> dependencyMap = new HashMap<>();
//                        for (String dependencyString : dependencies.values()) {
//                            dependencyMap.put(dependencyString, dependencyString);
//                        }
//
//                        dependencyMap.remove(className);
//
//                        JsonArray dependenciesJsonArray = parser.parse(gson.toJson(dependencyMap.values())).getAsJsonArray();
//                        classJson.add("dependencies", dependenciesJsonArray);
//                        break;
//                    }
//
//                } else if (classes.get(dependency) == null) {
//                    log.debug("dependency not in classes - " + dependency + " for - " + className);
//                }
//            }
//        }
//
//        return classes;
//    }
}
