package com.piranha.dist;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Created by root on 4/12/16.
 */
public class GraphShceduler {
    public static void main(String[] args) {

        DirectedGraph<String, DefaultEdge> testGraph =
                new DefaultDirectedGraph<>(DefaultEdge.class);

        Gson gson = new Gson();


        JsonObject obj1 = new JsonObject();
        obj1.addProperty("Name", "Object1");

        JsonObject obj2 = new JsonObject();
        obj1.addProperty("Name", "Object2");

        JsonObject obj3 = new JsonObject();
        obj1.addProperty("Name", "Object3");

        testGraph.addVertex("N1");
        testGraph.addVertex("N2");
        testGraph.addVertex("N3");
        testGraph.addVertex("N4");
        testGraph.addVertex("N5");

        testGraph.addEdge("N1","N2");
        testGraph.addEdge("N2","N3");
        testGraph.addEdge("N3","N1");

        testGraph.addEdge("N5","N3");
        testGraph.addEdge("N3","N3");

        //testGraph.addEdge(obj1,obj3);

        SzwarcfiterLauerSimpleCycles<String, DefaultEdge> detector1 = new SzwarcfiterLauerSimpleCycles<>(testGraph);
        System.out.println(detector1.findSimpleCycles());
        System.out.println("--------------------");
        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(testGraph);
        //System.out.println(detector.detectCycles());

        System.out.println(detector.findCyclesContainingVertex("N2"));
    }

    public static class Vertex{
        private String name;

        public Vertex(String name){
            this.name = name;
        }
        public String toString(){
            return name;
        }
    }
}
