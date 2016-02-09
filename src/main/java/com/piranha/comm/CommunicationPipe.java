package com.piranha.comm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.piranha.util.Communication;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.StringMatchFilter;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by Padmaka on 1/26/16.
 */
public class CommunicationPipe extends Thread{
    private static final Logger log = Logger.getLogger(CommunicationPipe.class);
    private ArrayList<String> nodes = new ArrayList<>();
    private int portNo;

    public CommunicationPipe(int portNo) {
        this.portNo = portNo;
    }

    @Override
    public void run() {
        Communication comm = new Communication();
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

                InetSocketAddress ipAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
                InetAddress inetAddress = ipAddress.getAddress();

                log.debug(inetAddress.getHostAddress());

                ServerSocket nodeCommLine = new ServerSocket(0);
                JsonObject portInfo = new JsonObject();

                portInfo.addProperty("portNo", nodeCommLine.getLocalPort());
                comm.writeToSocket(socket, portInfo);
                nodes.add(inetAddress.getHostAddress());
                log.debug("Node at " + nodeCommLine.getLocalPort() + " added");



            } catch (IOException e) {
                log.error("Error" , e);
            }
        }
    }

    public ArrayList<String> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<String> nodes) {
        this.nodes = nodes;
    }
}
