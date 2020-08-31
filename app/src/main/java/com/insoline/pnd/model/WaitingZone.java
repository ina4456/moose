package com.insoline.pnd.model;

public class WaitingZone {
    private int carId; // Car ID (4)
    private String waitingZoneId;
    private String waitingZoneName;
    private int numberOfCarsInAreas;    //전체 대기 인원
    private boolean isAvailableWait;
    private int myWaitingOrder;                //선택된 대기존에서 내 순번
    private float longitude; // 대기지역 경도 (30)
    private float latitude; // 대기지역 위도 (30)
    private int waitRange; // 대기범위 (2)


    public WaitingZone(int carId, String waitingZoneId, String waitingZoneName, int numberOfCarsInAreas, boolean isAvailableWait, int myWaitingOrder, float longitude, float latitude, int waitRange) {
        this.carId = carId;
        this.waitingZoneId = waitingZoneId;
        this.waitingZoneName = waitingZoneName;
        this.numberOfCarsInAreas = numberOfCarsInAreas;
        this.isAvailableWait = isAvailableWait;
        this.myWaitingOrder = myWaitingOrder;
        this.longitude = longitude;
        this.latitude = latitude;
        this.waitRange = waitRange;
    }


    public String getWaitingZoneId() {
        return waitingZoneId;
    }

    public void setWaitingZoneId(String waitingZoneId) {
        this.waitingZoneId = waitingZoneId;
    }

    public String getWaitingZoneName() {
        return waitingZoneName;
    }

    public void setWaitingZoneName(String waitingZoneName) {
        this.waitingZoneName = waitingZoneName;
    }

    public int getNumberOfCarsInAreas() {
        return numberOfCarsInAreas;
    }

    public void setNumberOfCarsInAreas(int numberOfCarsInAreas) {
        this.numberOfCarsInAreas = numberOfCarsInAreas;
    }

    public boolean isAvailableWait() {
        return isAvailableWait;
    }

    public void setAvailableWait(boolean availableWait) {
        isAvailableWait = availableWait;
    }

    public int getMyWaitingOrder() {
        return myWaitingOrder;
    }

    public void setMyWaitingOrder(int myWaitingOrder) {
        this.myWaitingOrder = myWaitingOrder;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public int getWaitRange() {
        return waitRange;
    }

    public void setWaitRange(int waitRange) {
        this.waitRange = waitRange;
    }

    @Override
    public String toString() {
        return "WaitingZone{" +
                "carId=" + carId +
                ", waitingZoneId='" + waitingZoneId + '\'' +
                ", waitingZoneName='" + waitingZoneName + '\'' +
                ", numberOfCarsInAreas=" + numberOfCarsInAreas +
                ", isAvailableWait=" + isAvailableWait +
                ", myWaitingOrder=" + myWaitingOrder +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", waitRange=" + waitRange +
                '}';
    }
}
