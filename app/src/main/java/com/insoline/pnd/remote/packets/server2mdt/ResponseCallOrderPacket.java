package com.insoline.pnd.remote.packets.server2mdt;

import com.insoline.pnd.remote.packets.Packets;
import com.insoline.pnd.remote.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 배차데이터 (GT-1312) 186 Byte (규격서에 나와 있는 +64 byte는 사용 안 한다.)
 * <p>
 * Edit by sbkwon 2017. 9. 19
 * 복지콜 추가. 따라서 규격서에 나온대로 +64 byte 사용하게 변경
 * <p>
 * Modified 2018. 12. 20
 * 배차데이터 목적지 추가 (GT-1311) 256 bytes 같이 사용한다.
 * <p>
 * Server -> MDT
 */
public class ResponseCallOrderPacket extends ResponsePacket {

    private static final int BYTE_LENGTH_FOR_DISABLED_PERSON = 250;
    private static final int BYTE_LENGTH_FOR_NORMAL = 186;
    private static final int BYTE_LENGTH_FOR_DISABLED_PERSON_WITH_DEST = 300;
    private static final int BYTE_LENGTH_FOR_NORMAL_WITH_DEST = 236;

    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private Packets.OrderKind orderKind; // 배차구분 (1)
    private String callReceiptDate; // 콜접수일자(ex : 2009-01-23) (11)
    private int callNumber; // 콜번호 (2)
    private float longitude; // 고객 경도 (4)
    private float latitude; // 고객 위도 (4)
    private String callerPhone; // 고객 연락처 (13)
    private String place; // 탑승지 (41)
    private String placeExplanation; // 탑승지 설명 (101)
    private int allocBoundary; // 배차범위 (2)
    private int orderCount; // 배차횟수 (1)
//    private String callerName; // 고객이름 (11)
//    private String errorCode; // 장애코드 (11)
//    private String destination; // 목적지 (41)
//    private boolean isWheelChair; // 휠체어여부 (1)

    private String destName;// 목적지 장소명 (41)
    private float destLongitude; // 목적지 경도 (4)
    private float destLatitude; // 목적지 위도 (4)
    private int callClass; // 콜 등급 (1)

    private boolean isReported; // 운행보고 여부 (Local에서만 사용하는 값이다.)

    private boolean isCallForDisabledPerson; //복지콜 여부

    public ResponseCallOrderPacket(byte[] bytes) {
        super(bytes);
    }

    public ResponseCallOrderPacket(ResponseCallInfoPacket p) {
        super(new byte[(p.isCallForDisabledPerson() ? BYTE_LENGTH_FOR_DISABLED_PERSON_WITH_DEST : BYTE_LENGTH_FOR_NORMAL_WITH_DEST)]);

        corporationCode = p.getCorporationCode();
        carId = p.getCarId();
        orderKind = p.getOrderKind();
        callReceiptDate = p.getCallReceiptDate();
        callNumber = p.getCallNumber();
        longitude = p.getLongitude();
        latitude = p.getLatitude();
        callerPhone = p.getCallerPhone();
        place = p.getPlace();
        placeExplanation = p.getPlaceExplanation();
        allocBoundary = p.getAllocBoundary();
        orderCount = p.getOrderCount();

        destName = p.getDestName();
        destLongitude = p.getDestLongitude();
        destLatitude = p.getDestLatitude();
        callClass = p.getCallClass();
    }

    public int getCorporationCode() {
        return corporationCode;
    }

    public int getCarId() {
        return carId;
    }

    public Packets.OrderKind getOrderKind() {
        return orderKind;
    }

    public String getCallReceiptDate() {
        return callReceiptDate;
    }

    public int getCallNumber() {
        return callNumber;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public String getCallerPhone() {
        return callerPhone;
    }

    public String getPlace() {
        return place;
    }

    public String getPlaceExplanation() {
        return placeExplanation;
    }

    public int getAllocBoundary() {
        return allocBoundary;
    }

    public int getOrderCount() {
        return orderCount;
    }

//    public String getCallerName() {
//        return callerName;
//    }
//
//    public String getErrorCode() {
//        return errorCode;
//    }
//
//    public String getDestination() {
//        return destination;
//    }
//
//    public String isWheelChair() {
//        return isWheelChair ? "Y" : "N";
//    }

    public String getDestName() {
        return destName;
    }

    public float getDestLongitude() {
        return destLongitude;
    }

    public float getDestLatitude() {
        return destLatitude;
    }

    public int getCallClass() {
        return callClass;
    }

    public boolean isCallForDisabledPerson() {
        return isCallForDisabledPerson;
    }

    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        corporationCode = readInt(2);
        carId = readInt(2);
        int order = readInt(1);
        if (Packets.OrderKind.Normal.value == order) {
            orderKind = Packets.OrderKind.Normal;
        } else if (Packets.OrderKind.Wait.value == order) {
            orderKind = Packets.OrderKind.Wait;
        } else if (Packets.OrderKind.Forced.value == order) {
            orderKind = Packets.OrderKind.Forced;
        } else if (Packets.OrderKind.Manual.value == order) {
            orderKind = Packets.OrderKind.Manual;
        } else if (Packets.OrderKind.WaitOrder.value == order) {
            orderKind = Packets.OrderKind.WaitOrder;
        } else if (Packets.OrderKind.WaitOrderTwoWay.value == order) {
            orderKind = Packets.OrderKind.WaitOrderTwoWay;
        } else if (Packets.OrderKind.GetOnOrder.value == order) {
            orderKind = Packets.OrderKind.GetOnOrder;
        } else if (Packets.OrderKind.Mobile.value == order) {
            orderKind = Packets.OrderKind.Mobile;

            //2018. 01. 03 - 권석범
            //통합배차가 내려왔을 경우 정의가 되어 있지 않아 앱이 중지가 되는 증상이 있어 추가하고
            //그 외 없는 배차가 내려올시에 일반 배차로 지정함 (김용태 팀장 요청)
        } else if (Packets.OrderKind.Integration.value == order) {
            orderKind = Packets.OrderKind.Integration;
        } else {
            orderKind = Packets.OrderKind.Normal;
        }
        callReceiptDate = readString(11);
        callNumber = readInt(2);
        longitude = readFloat(4);
        latitude = readFloat(4);
        callerPhone = readString(13);
        place = readString(41);
        placeExplanation = readString(101);
        allocBoundary = readInt(2);
        orderCount = readInt(1);

        destName = readString(41);
        destLongitude = readFloat(4);
        destLatitude = readFloat(4);
        callClass = readInt(1);
    }

    @Override
    public String toString() {
        return "배차데이터 목적지 추가 (0x" + Integer.toHexString(messageType) + ") " +
                "corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", orderKind=" + orderKind +
                ", callReceiptDate='" + callReceiptDate + '\'' +
                ", callNumber=" + callNumber +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", callerPhone='" + callerPhone + '\'' +
                ", place='" + place + '\'' +
                ", placeExplanation='" + placeExplanation + '\'' +
                ", allocBoundary=" + allocBoundary +
                ", orderCount=" + orderCount +
                ", destName='" + destName + '\'' +
                ", destLongitude='" + destLongitude + '\'' +
                ", destLatitude='" + destLatitude + '\'' +
                ", callClass=" + callClass +
                ", isReported=" + isReported;
    }
}