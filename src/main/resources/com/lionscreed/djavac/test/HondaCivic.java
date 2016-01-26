package com.lionscreed.djavac.test;

/**
 * Created by Padmaka on 8/27/2015.
 */
public class HondaCivic extends Car {

    public HondaCivic(){
        this.model = "Civic";
        this.make = "Honda";
        this.bodyType = "Car-Sedan";
    }

    @Override
    public String getModel() {
        return this.model;
    }

    @Override
    public String getMake() {
        return this.make;
    }

    @Override
    public String getBodyType() {
        return this.bodyType;
    }

    @Override
    public void doSomething() {

    }
}
