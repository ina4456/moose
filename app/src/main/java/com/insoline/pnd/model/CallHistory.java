package com.insoline.pnd.model;

public class CallHistory {
    private int callType;
    private String callTypeStr;
    private int callId;
    private String date;
    private String departure;
    private String destination;
    private String startTime;
    private String endTime;
    private String passengerPhoneNumber;

    public CallHistory(int callType, String callTypeStr, int callId, String date, String departure, String destination, String startTime, String endTime, String passengerPhoneNumber) {
        this.callType = callType;
        this.callTypeStr = callTypeStr;
        this.callId = callId;
        this.date = date;
        this.departure = departure;
        this.destination = destination;
        this.startTime = startTime;
        this.endTime = endTime;
        this.passengerPhoneNumber = passengerPhoneNumber;
    }

    public int getCallId() {
        return callId;
    }

    public void setCallId(int callId) {
        this.callId = callId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCallTypeStr() {
        return callTypeStr;
    }

    public void setCallTypeStr(String callTypeStr) {
        this.callTypeStr = callTypeStr;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPassengerPhoneNumber() {
        return passengerPhoneNumber;
    }

    public void setPassengerPhoneNumber(String passengerPhoneNumber) {
        this.passengerPhoneNumber = passengerPhoneNumber;
    }

    @Override
    public String toString() {
        return "CallHistory{" +
                "date='" + date + '\'' +
                ", callTypeStr='" + callTypeStr + '\'' +
                ", departure='" + departure + '\'' +
                ", destination='" + destination + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", passengerPhoneNumber='" + passengerPhoneNumber + '\'' +
                '}';
    }
}
