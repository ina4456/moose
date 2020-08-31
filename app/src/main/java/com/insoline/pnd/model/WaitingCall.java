package com.insoline.pnd.model;

/**
 * Created by seok-beomkwon on 2017. 10. 24..
 */

public class WaitingCall {
	private String date;
	private String compCode;
	private String orderSeq;
	private String departureAddress;
	private String departurePoi;
	private String destinationAddress;
	private String destinationPoi;
	private String longitude;
	private String latitude;

	private String callDistanceToDeparture;
	private float distance;


	public WaitingCall(String date, String compCode, String orderSeq, String departureAddress, String departurePoi, String destinationPoi, String longitude, String latitude) {
		this.date = date;
		this.compCode = compCode;
		this.orderSeq = orderSeq;
		this.departureAddress = departureAddress;
		this.departurePoi = departurePoi;
		this.destinationPoi = destinationPoi;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCompCode() {
		return compCode;
	}

	public void setCompCode(String compCode) {
		this.compCode = compCode;
	}

	public String getOrderSeq() {
		return orderSeq;
	}

	public void setOrderSeq(String orderSeq) {
		this.orderSeq = orderSeq;
	}

	public String getDepartureAddress() {
		return departureAddress;
	}

	public void setDepartureAddress(String departureAddress) {
		this.departureAddress = departureAddress;
	}

	public String getDeparturePoi() {
		return departurePoi;
	}

	public void setDeparturePoi(String departurePoi) {
		this.departurePoi = departurePoi;
	}

	public String getDestinationPoi() {
		return destinationPoi;
	}

	public void setDestinationPoi(String destinationPoi) {
		this.destinationPoi = destinationPoi;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getCallDistanceToDeparture() {
		return callDistanceToDeparture;
	}

	public void setCallDistanceToDeparture(String callDistanceToDeparture) {
		this.callDistanceToDeparture = callDistanceToDeparture;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
		String distanceStr;
		float distanceTemp;

		if (distance < 1000) {
			distanceStr = (int)distance + "m";
		} else {
			distanceTemp = distance / 1000;
			distanceTemp = distanceTemp + ((distance - (distanceTemp * 1000)) / 1000);
			distanceStr = String.format("%.1f", distanceTemp);
			distanceStr = distanceStr + "km";
		}
		this.setCallDistanceToDeparture(distanceStr);
	}

	@Override
	public String toString() {
		return "WaitingCall{" +
				"date='" + date + '\'' +
				", compCode='" + compCode + '\'' +
				", orderSeq='" + orderSeq + '\'' +
				", departureAddress='" + departureAddress + '\'' +
				", departurePoi='" + departurePoi + '\'' +
				", destinationAddress='" + destinationAddress + '\'' +
				", destinationPoi='" + destinationPoi + '\'' +
				", longitude='" + longitude + '\'' +
				", latitude='" + latitude + '\'' +
				'}';
	}
}
