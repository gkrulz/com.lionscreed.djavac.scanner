package com.piranha.dist;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.comm.CommunicationPipe;
import com.piranha.comm.HttpUtils;
import com.piranha.util.Communication;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Padmaka on 1/24/16.
 */
public class Distributor {
    private static final Logger log = Logger.getLogger(Distributor.class);
    private CommunicationPipe communicationPipe;
    private Communication comm;

    public Distributor (CommunicationPipe communicationPipe) {
        this.communicationPipe = communicationPipe;
        this.comm = new Communication();
    }

    public ArrayList<ArrayList<List<JsonObject>>> makeDistributionPlan(ArrayList<ArrayList<JsonObject>> schedule) throws SocketException {
        ArrayList<ArrayList<List<JsonObject>>> distributionPlan = new ArrayList<>();
        ArrayList<String> nodes = this.communicationPipe.getNodes();

        for (ArrayList<JsonObject> round : schedule) {
            int noOfClassesPerNode = (round.size() / nodes.size()) == 0 ? 1 : round.size() / nodes.size();
            int noOfIterations = 0;

            log.debug("No of classes in round " + round.size());

            if ((round.size() % nodes.size()) == 0){
                noOfIterations = noOfClassesPerNode * nodes.size();
            } else if ((round.size() % nodes.size()) != 0 && round.size() < nodes.size()) {
                noOfIterations = noOfClassesPerNode;
            } else if ((round.size() % nodes.size()) != 0 && round.size() > nodes.size()) {
                noOfIterations = (noOfClassesPerNode * nodes.size()) + noOfClassesPerNode;
            }

            List<JsonObject> newList = null;
            ArrayList<List<JsonObject>> roundList = new ArrayList<>();

            for (int i = 0; i < noOfIterations; i = i + noOfClassesPerNode) {
                int upperLimit = ((i + noOfClassesPerNode) > round.size()) ? round.size() : (i + noOfClassesPerNode);

                newList = round.subList(i, upperLimit);
                log.debug(newList);
                roundList.add(newList);
            }

            if (newList != null) {
                distributionPlan.add(roundList);
            }
        }

        log.debug(distributionPlan);
        return distributionPlan;
    }

    /***
     * The method to distribute classes for compilation
     * @param distributionPlan
     * @throws IOException
     */
    public void distribute (ArrayList<ArrayList<List<JsonObject>>> distributionPlan) throws IOException {
        int noOfNodes = this.communicationPipe.getNodes().size();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        HashMap<String, String> dependencyMap = new HashMap<>();
        int x = 0;

        for (ArrayList<List<JsonObject>> round : distributionPlan) {
            int noOfIterations = 0;

            if (round.size() > noOfNodes) {
                noOfIterations = noOfNodes;
            } else if (round.size() < noOfNodes) {
                noOfIterations = round.size();
            } else {
                noOfIterations = noOfNodes;
            }

            for (int i = 0; i < noOfIterations; i++) {

                if (i == (noOfNodes - 1) && round.size() > noOfNodes) {
                    round.get(i).addAll(round.get(i + 1));
                }

                String ipAddress = this.communicationPipe.getNodes().get(i);
                log.debug(ipAddress);
                //Socket socket = new Socket(ipAddress, 9006);
                HttpPost post = new HttpPost();


                JsonObject compilationWorkloadJson = new JsonObject();
                compilationWorkloadJson.addProperty("op", "COMPILATION");
                compilationWorkloadJson.addProperty("classes", gson.toJson(round.get(i)));
                compilationWorkloadJson.addProperty("dependencyMap", gson.toJson(dependencyMap));

                post.setEntity(new StringEntity(compilationWorkloadJson.toString()));

                HttpUtils.doRequest(post);

                //this.comm.writeToSocket(socket, compilationWorkloadJson);
                //socket.close();

                for (JsonObject classJson : round.get(i)) {
                    if (ipAddress.equals("127.0.0.1")) {
                        InetAddress networkIpAddress = InetAddress.getLocalHost();
                        ipAddress = networkIpAddress.getHostAddress();
                    }

                    dependencyMap.put(classJson.get("absoluteClassName").getAsString(), ipAddress);
                }

                log.debug(round.get(i));
            }
            x++;
        }

        //Send the compilation termination message
        JsonObject terminationMessage = new JsonObject();

        terminationMessage.addProperty("op", "TERMINATE");
        terminationMessage.addProperty("classes", gson.toJson(dependencyMap));

        for (String ipAddress : this.communicationPipe.getNodes()) {
            Socket socket = new Socket(ipAddress, 9006);
            this.comm.writeToSocket(socket, terminationMessage);
            log.debug(gson.toJson(terminationMessage));
        }
    }
}
