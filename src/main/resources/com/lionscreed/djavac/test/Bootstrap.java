package com.lionscreed.djavac.test;

/**
 * Created by Padmaka on 8/27/2015.
 */
public class Bootstrap implements Stopable{

    public static void main(String[] args) {
        Vehicle corolla = new ToyotaCorolla();
        Vehicle civic = new HondaCivic();
        Vehicle kdh = new ToyotaKDH();

        System.out.println(corolla.getMake());
        System.out.println(corolla.getModel());
        System.out.println(corolla.bodyType+"\n");

        System.out.println(civic.getMake());
        System.out.println(civic.getModel());
        System.out.println(civic.bodyType+"\n");

        System.out.println(kdh.getMake());
        System.out.println(kdh.getModel());
        System.out.println(kdh.bodyType);
    }

    public void doSomething(){

    }

    class abc {
        private int no = 12;
    }
}
