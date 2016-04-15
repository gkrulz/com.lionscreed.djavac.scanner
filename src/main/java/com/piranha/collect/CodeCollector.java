package com.piranha.collect;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.piranha.util.Communication;
import com.piranha.util.Constants;
import com.piranha.util.PiranhaConfig;
import com.sun.org.apache.bcel.internal.classfile.Code;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Padmaka on 4/14/16.
 */
public class CodeCollector extends Thread {
    private static final Logger log = Logger.getLogger(CodeCollector.class);

    private ServerSocket serverSocket;
    private ExecutorService service;
    private ConcurrentHashMap<String, String> receivedFiles;
    private ConcurrentHashMap<String, String> filesToReceive;
    private boolean allFilesReceived;
    private int numberOfClients;
    private long startedTime;

    public CodeCollector(int numberOfClients, ConcurrentHashMap<String, String> filesToReceive, long startedTime) {
        this.numberOfClients = numberOfClients;
        this.filesToReceive = filesToReceive;
        this.startedTime = startedTime;
    }

    public void run() {
        try {
            allFilesReceived = false;
            service = Executors.newFixedThreadPool(4);
            serverSocket = new ServerSocket(2500);
            receivedFiles = new ConcurrentHashMap<>();

            boolean allNodesConnected = false;
            ArrayList<Socket> connectedNodes = new ArrayList<>();
            log.debug("NUMBER OF CLIENTS - "+numberOfClients);
            while (!allNodesConnected) {
                Socket client = serverSocket.accept();
                FileAcceptor acceptor = new FileAcceptor(client,receivedFiles);
                service.submit(acceptor);
                connectedNodes.add(client);

                if (connectedNodes.size() >= numberOfClients) {
                    allNodesConnected = true;
                }
            }

            while (true) {
                log.debug("RECEIVED FILE SIZE - " + receivedFiles.size());

                if (filesToReceive.equals(receivedFiles)) {
                    long timeNow = System.nanoTime();
                    log.debug("All Files Compiled in " + ((timeNow - startedTime) / 1000000000.00) + " Seconds");
                    log.debug("RECEIVED FILE SIZE - " + receivedFiles.size());
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static class FileAcceptor extends Thread {

        private Socket client;
        private ConcurrentHashMap<String, String> receivedFiles;


        public FileAcceptor(Socket client, ConcurrentHashMap<String, String> receivedFiles){
            this.client = client;
            this.receivedFiles = receivedFiles;
        }

        public void run() {
            Communication comm = new Communication();
            JsonParser parser = new JsonParser();

            try {
                String[] response = comm.readFromSocket(client);

                for (int i = 0; i < response.length; i++) {
                    String className = parser.parse(response[i]).getAsJsonObject().get("className").getAsString();
                    String fileName = parser.parse(response[i]).getAsJsonObject().get("className").getAsString();
                    fileName = fileName.replace(".", Constants.PATH_SEPARATOR);

                    File file = new File(PiranhaConfig.getProperty("DESTINATION_PATH") + Constants.PATH_SEPARATOR + fileName + ".class");
                    file.getParentFile().mkdirs();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    byte[] bytes = Base64.decodeBase64(parser.parse(response[i]).getAsJsonObject().get("file").getAsString());

                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

                    IOUtils.copy(bis, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    client.close();
                    receivedFiles.put(className,className);
                    log.debug("Dependency " + className + " received");
                }
            } catch (IOException e) {
                log.error("Error", e);
            } catch (ClassNotFoundException e) {
                log.error("Error", e);
            }
        }
    }
}
