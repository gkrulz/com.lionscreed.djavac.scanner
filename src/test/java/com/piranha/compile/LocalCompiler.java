package com.piranha.compile;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.piranha.comm.CommunicationPipe;
import com.piranha.dist.Distributor;
import com.piranha.dist.Scheduler;
import com.piranha.scan.Scanner;
import com.piranha.scan.ScannerX;
import com.piranha.scan.test.ConcurrentHashmapTest;
import com.piranha.util.Constants;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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



        log.debug(gson.toJson(classes));

        long endTime = System.nanoTime();

        long timeTaken = endTime - startTime;

        double seconds = (double)timeTaken / 1000000000.0;
        log.info(seconds + "s");

//        Scheduler scheduler = new Scheduler();
//        ArrayList<ArrayList<JsonObject>> schedule = scheduler.makeSchedule(classes);
//
//        //compilation test
//        Compiler compiler = null;
//        try {
//            compiler = new Compiler(Constants.DESTINATION_PATH);
//        } catch (IOException e) {
//            log.error("Unable to initialize compiler", e);
//        }
//
//        long startTime = System.nanoTime();
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
//        long endTime = System.nanoTime();
//
//        long timeTaken = endTime - startTime;
//
//        double seconds = (double)timeTaken / 1000000000.0;
//        log.info(seconds + "s");
    }
}