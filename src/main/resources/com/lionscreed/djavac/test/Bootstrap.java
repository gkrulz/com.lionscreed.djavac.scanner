package com.lionscreed.djavac.test;

import com.lionscreed.djavac.acomp.*;

/**
 * Created by Padmaka on 8/27/2015.
 */
public class Bootstrap extends Compiler implements com.lionscreed.djavac.test.Stopable {

    /* This is
     a comment */

    public static void main(String[] args) {
        Vehicle corolla = new com.lionscreed.djavac.test.ToyotaCorolla();
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
        System.out.println("sdfsdf");
    }

    public void test(){

    }

    class abc {
        private int no = 12;
    }
}

class OuterClass {
    int bla = 123;
}

enum testEnum implements Runnable {

    FIRST, SECOND;

    @Override
    public void run() {

    }
}