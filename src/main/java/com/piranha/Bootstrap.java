package com.piranha;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.piranha.comm.CommunicationPipe;
import com.piranha.dist.Distributor;
import com.piranha.dist.Scheduler;
import com.piranha.scan.Scanner;
import com.piranha.util.Communication;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Padmaka on 8/25/2015.
 */
public class Bootstrap {
    private static final Logger log = Logger.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        ArrayList<JsonObject> classes;
        ArrayList<JsonObject> detailedClassList;

        // Listening for other nodes to connect
        //----------------------------------------------------------------------
       CommunicationPipe communicationPipe = new CommunicationPipe(9005);

        communicationPipe.start();
        while (true) {
            if (communicationPipe.getNodes().size() > 1) {
                break;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                log.error("Error", e);
            }
        }
        //----------------------------------------------------------------------

        Scanner scanner = new Scanner();
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
        detailedClassList = scanner.getDetailedClassList();

        classes = scanner.removeUnnecessaryImportStatements();

        Scheduler scheduler = new Scheduler();
        ArrayList<ArrayList<JsonObject>> schedule = scheduler.makeSchedule(classes);

        ArrayList<ArrayList<List<JsonObject>>> distributionPlan = new ArrayList<>();

        Distributor distributor = new Distributor(communicationPipe);
        try {
            distributionPlan = distributor.makeDistributionPlan(schedule);
        } catch (SocketException e) {
            log.error("Error", e);
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.error("Error", e);
        }

        try {
            distributor.distribute(distributionPlan);
        } catch (IOException e) {
            log.error("Error", e);
        }

        //compilation test
//        Compiler compiler = new Compiler("/Users/Padmaka/Desktop");
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
    }
}