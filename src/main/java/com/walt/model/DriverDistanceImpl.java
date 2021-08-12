package com.walt.model;


public class DriverDistanceImpl implements DriverDistance{

    private Driver driver;
    private Long distance;
    public DriverDistanceImpl(Driver driver, Long distance){
        this.driver = driver;
        this.distance = distance;
    }

    @Override
    public Driver getDriver() {
        return driver;
    }

    @Override
    public Long getTotalDistance() {
        return distance;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Long getDistance() {
        return distance;
    }

    public void setDistance(Long distance) {
        this.distance = distance;
    }
}
