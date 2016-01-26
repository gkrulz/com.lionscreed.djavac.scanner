package com.piranha.comm;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
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
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String nodeId = bufferedReader.readLine();
                getNodes().add(socket);
                log.debug("Node " + nodeId + " added");
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
}
