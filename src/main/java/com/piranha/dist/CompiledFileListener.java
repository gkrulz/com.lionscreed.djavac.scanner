package com.piranha.dist;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.util.Communication;
import com.piranha.util.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by root on 4/13/16.
 */
public class CompiledFileListener extends Thread {

    private static final Logger log = Logger.getLogger(CompiledFileListener.class);

    private int threadPoolSize;
    private int port;
    private ExecutorService executorService;
    private AtomicBoolean isRunning = new AtomicBoolean(true);

    public CompiledFileListener(int threadPoolSize, int port) {
        this.threadPoolSize = threadPoolSize;
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (isRunning.get()) {
                Socket client = serverSocket.accept();
                log.debug("Accepted Compiled file Receiving from"+client.getRemoteSocketAddress());
                CompiledFileAcceptor listener = new CompiledFileAcceptor(client);
                executorService.submit(listener);
            }
        } catch (IOException e) {
            e.printStackTrace();//TODO logg
        }


    }

    public void shutdown(){
        if(executorService!= null){
            executorService.shutdown();
        }
        isRunning = new AtomicBoolean(false);
    }

    private static class CompiledFileAcceptor implements Runnable {

        private static final Logger log = Logger.getLogger(CompiledFileAcceptor.class);

        private Socket socket;
        private Properties properties;
        private String targetFolder;

        public CompiledFileAcceptor(Socket socket) {
            this.socket = socket;
            properties = new Properties();
            try {
                properties.load(CompiledFileAcceptor.class.getClassLoader().getResourceAsStream("config.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            Communication comm = new Communication();
            JsonParser parser = new JsonParser();

            try {
                JsonObject responseJson = parser.parse(comm.readFromSocket(socket)).getAsJsonObject();

                String fileName = responseJson.get("className").getAsString();
                fileName = fileName.replace("/", Constants.PATH_SEPARATOR);
                fileName = fileName.replace("\\", Constants.PATH_SEPARATOR);

                File file = new File(targetFolder + fileName);
                file.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                byte[] bytes = Base64.decodeBase64(responseJson.get("file").getAsString());

                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

                IOUtils.copy(bis, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                log.debug("Written Compiled file -"+responseJson.get("className").getAsString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
