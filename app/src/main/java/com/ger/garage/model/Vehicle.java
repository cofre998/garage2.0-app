package com.ger.garage.model;

public class Vehicle {

    private String numberPlate;

    public Vehicle() {}

    public Vehicle(String numberPlate) {
        this.numberPlate = numberPlate;
    }

    public String getNumberPlate() {
        return numberPlate;
    }

    public void setNumberPlate(String numberPlate) {
        this.numberPlate = numberPlate;
    }

    @Override
    public String toString() {
        return numberPlate;
    }
}