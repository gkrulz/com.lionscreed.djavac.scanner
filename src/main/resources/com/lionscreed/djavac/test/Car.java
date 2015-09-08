package com.lionscreed.djavac.test;

/**
 * Created by Padmaka on 8/27/2015.
 */
public abstract class Car extends Vehicle implements Stopable {

    public Car(){
        this.model = "Car";
        this.make = "Generic Car";
        this.bodyType = "Car";
    }
}
