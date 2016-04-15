package com.piranha;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.piranha.collect.CodeCollector;
import com.piranha.comm.CommunicationPipe;
import com.piranha.dist.Distributor;
import com.piranha.dist.Scheduler;
import com.piranha.scan.Scanner;
import com.piranha.scan.ScannerX;
import com.piranha.util.PiranhaConfig;
import com.piranha.util.Utils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Padmaka on 8/25/2015.
 */
public class Bootstrap {
    private static final Logger log = Logger.getLogger(Bootstrap.class);


    public static void main(String[] args) {
        try {
            PiranhaConfig.innitializeProperties();
        } catch (IOException e) {
            log.error("Unable to initialize config", e);
        }
        log.debug(PiranhaConfig.getProperty("NO_OF_NODES"));
        int nodes = Integer.parseInt(PiranhaConfig.getProperty("NO_OF_NODES"));

        ConcurrentHashMap<String, JsonObject> classes;
        ArrayList<JsonObject> detailedClassList;

        // Listening for other nodes to connect
        //----------------------------------------------------------------------
        CommunicationPipe communicationPipe = new CommunicationPipe(9005);

        int numOfNodes = 0;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9005);
        } catch (IOException e) {
            log.error("", e);
        }

        String ip = null;
        Socket socket = null;
//        communicationPipe.start();
        while (numOfNodes < nodes) {
            try {
                socket = serverSocket.accept();
                ip = socket.getInetAddress().getHostAddress();
                numOfNodes = numOfNodes + 1;
                communicationPipe.getNodes().add(ip);
                log.debug(ip);
            } catch (IOException e) {
                log.error("", e);
            }
        }
        //--------------------------Termination Listener------------------------

//        int threadPoolSize = 4;
//        CompiledFileListener listener = new CompiledFileListener(threadPoolSize,10000);
//        listener.start();



        //----------------------------------------------------------------------
        ScannerX scanner = null;
        try {
            scanner = new ScannerX();
        } catch (IOException e) {
            log.error("Unable to initialize scannerX");
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

        Scheduler scheduler = new Scheduler();
        ArrayList<ArrayList<JsonObject>> schedule = scheduler.makeScheduleTemp(classes);

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

        ConcurrentHashMap<String, String> classToBeCompiled = new ConcurrentHashMap<>();

        for (ArrayList<JsonObject> round : schedule){
            for (JsonObject classJson : round){
                JsonArray toBeCompiledWith = null;
                String name = classJson.get("absoluteClassName").getAsString();

                if (classJson.get("toBeCompiledWith") != null){
                    toBeCompiledWith = classJson.get("toBeCompiledWith").getAsJsonArray();

                    for (JsonElement element : toBeCompiledWith){
                        String className = element.getAsJsonObject().get("absoluteClassName").getAsString();

                        classToBeCompiled.put(className, className);
                    }
                }

                classToBeCompiled.put(name, name);
            }
        }

        log.debug(classToBeCompiled.values().size());

        long startTime = System.nanoTime();
        CodeCollector codeCollector = new CodeCollector(nodes, classToBeCompiled, startTime);
        codeCollector.start();
    }
}