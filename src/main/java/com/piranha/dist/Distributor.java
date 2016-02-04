package com.piranha.dist;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Padmaka on 1/24/16.
 */
public class Distributor {
    private static final Logger log = Logger.getLogger(Distributor.class);

    public void distribute(ArrayList<SocketAddress> nodes, ArrayList<ArrayList<JsonObject>> schedule) throws SocketException {
        ArrayList<ArrayList<List<JsonObject>>> distributionPlan = new ArrayList<>();

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
    }
}
