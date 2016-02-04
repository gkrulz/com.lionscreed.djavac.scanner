package com.piranha.dist;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Padmaka on 1/24/16.
 */
public class Distributor {
    private static final Logger log = Logger.getLogger(Distributor.class);

    public void distribute(ArrayList<SocketAddress> nodes, ArrayList<ArrayList<JsonObject>> schedule) throws SocketException {

        for (ArrayList<JsonObject> round : schedule) {
            int noOfClassesPerNode = (round.size() / nodes.size()) == 0 ? 1 : round.size() / nodes.size();

            for (SocketAddress node : nodes) {
                if (node)
            }
        }
    }
}
