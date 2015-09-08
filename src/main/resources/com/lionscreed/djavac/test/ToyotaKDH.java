package com.lionscreed.djavac.test;

/**
 * Created by Padmaka on 8/27/2015.
 */
public class ToyotaKDH extends Van {

    public ToyotaKDH(){
        this.model = "KDH";
        this.make = "Toyota";
        this.bodyType = "Van-long";
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
}
