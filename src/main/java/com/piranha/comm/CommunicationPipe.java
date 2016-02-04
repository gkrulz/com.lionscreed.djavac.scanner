package com.piranha.comm;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by Padmaka on 1/26/16.
 */
public class CommunicationPipe extends Thread{
    private static final Logger log = Logger.getLogger(CommunicationPipe.class);
    private ArrayList<Socket> nodes = new ArrayList<>();
    private int portNo;

    public CommunicationPipe(int portNo) {
        this.portNo = portNo;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.portNo);
        } catch (IOException e) {
            log.error("Error", e);
        }

        log.info("Listener for nodes stared...");

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());

                ServerSocket nodeCommLine = new ServerSocket(0);
                JsonObject portInfo = new JsonObject();

                portInfo.addProperty("portNo", nodeCommLine.getLocalPort());
                this.writeToOutputStream(socket, portInfo);
                log.debug("Node at " + nodeCommLine.getLocalPort() + " added");

            } catch (IOException e) {
                log.error("Error" , e);
            }
        }
    }

    public ArrayList<Socket> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Socket> nodes) {
        this.nodes = nodes;
    }

    public void writeToOutputStream (Socket socket, JsonObject data) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        out.write(data.toString());
        out.flush();
        out.close();
    }
}
