package com.piranha.compile;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.piranha.dist.Scheduler;
import com.piranha.scan.ScannerX;
import com.piranha.util.Constants;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Padmaka on 8/25/2015.
 */
public class LocalCompiler {
    private static final Logger log = Logger.getLogger(LocalCompiler.class);

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        ConcurrentHashMap<String, JsonObject> classes;
        Gson gson = new Gson();

        ScannerX scanner = null;
        try {
            scanner = new ScannerX();
        } catch (IOException e) {
            log.error("Unable to initialize ScannerX", e);
        }
        Collection fileCollection = scanner.readFiles();
        ArrayList<File> files = new ArrayList<File>();

        for(Object obj :fileCollection){
            File f = (File)obj;
            files.add(f);
        }

        try {
            classes = scanner.scan(files);
        } catch (IOException e) {
            e.printStackTrace();
        }

        classes = scanner.findDependencies();
//        detailedClassList = scanner.getDetailedClassList();

        classes = scanner.removeUnnecessaryImportStatements();



//        log.debug(gson.toJson(classes));

        long endTime = System.nanoTime();

        long timeTaken = endTime - startTime;

        double seconds = (double)timeTaken / 1000000000.0;
        log.info(seconds + "s");

        Scheduler scheduler = new Scheduler();
//        ArrayList<ArrayList<JsonObject>> schedule = scheduler.makeSchedule(classes);

        ArrayList<ArrayList<JsonObject>> schedule = scheduler.makeScheduleTemp(classes);
//
//        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
//
//        for (JsonObject classJson : classes.values()){
//            ArrayList<String> dependencies = gson.fromJson(classJson.get("dependencies").getAsJsonArray(), listType);
//            String className = classJson.get("absoluteClassName").getAsString();
//
//            for (String dependency : dependencies) {
//
//                if (classes.get(dependency) != null) {
//                    JsonObject dependencyClassObj = classes.get(dependency).getAsJsonObject();
//                    String dependencyClassName = dependencyClassObj.get("absoluteClassName").getAsString();
//                    ArrayList<String> dependencyList = gson.fromJson(dependencyClassObj.get("dependencies").getAsJsonArray(), listType);
//
//                    if (dependencyList.contains(className)) {
//                        log.debug(className + " dependency - " + dependencyClassName);
//                        break;
//                    }
//
//                } else if (classes.get(dependency) == null){
//                    log.debug("dependency not in classes - " + dependency + " for - " + className);
//                }
//            }
//        }


        //compilation test
//        Compiler compiler = null;
//        try {
//            compiler = new Compiler(Constants.DESTINATION_PATH);
//        } catch (IOException e) {
//            log.error("Unable to initialize compiler", e);
//        }
//
//        long startTime2 = System.nanoTime();
//
//        for (ArrayList<JsonObject> currentRound : schedule) {
//            for (JsonObject currentClass : currentRound) {
//                StringBuilder packageName = new StringBuilder(currentClass.get("package").getAsString());
//                StringBuilder classString = new StringBuilder("package " + packageName.replace(packageName.length()-1, packageName.length(), "") + ";\n");
//
//                for (JsonElement importStatement : currentClass.get("importStatements").getAsJsonArray()) {
//                    classString.append("import " + importStatement.getAsString() + ";\n");
//                }
//                classString.append(currentClass.get("classDeclaration").getAsString());
//                classString.append(currentClass.get("classString").getAsString() + "}");
//                try {
//                    compiler.compile(currentClass.get("className").getAsString(), classString.toString());
//                } catch (Exception e) {
//                    log.error("", e);
//                }
////                log.debug(classString);
//            }
//        }
//
//        long endTime2 = System.nanoTime();
//
//        long timeTaken2 = endTime2 - startTime2;
//
//        double seconds2 = (double)timeTaken2 / 1000000000.0;
//        log.info(seconds2 + "s");
    }
}