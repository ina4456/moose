package com.insoline.pnd.remote.packets.mdt2server;

import com.insoline.pnd.remote.packets.Packets;
import com.insoline.pnd.remote.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 대기배차고객정보 요청 (GT-1517) 7 Byte
 * MDT -> Server
 */
public class RequestWaitCallInfoPacket extends RequestPacket {

    private int serviceNumber; // 서비스번호 (1)
    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)

    public RequestWaitCallInfoPacket() {
        super(Packets.REQUEST_WAIT_CALL_INFO);
    }

    public int getServiceNumber() {
        return serviceNumber;
    }

    public void setServiceNumber(int serviceNumber) {
        this.serviceNumber = serviceNumber;
    }

    public int getCorporationCode() {
        return corporationCode;
    }

    public void setCorporationCode(int corporationCode) {
        this.corporationCode = corporationCode;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(serviceNumber, 1);
        writeInt(corporationCode, 2);
        writeInt(carId, 2);
        return buffers;
    }

    @Override
    public String toString() {
        return "대기배차고객정보 요청 (0x" + Integer.toHexString(messageType) + ") " +
                "serviceNumber=" + serviceNumber +
                ", corporationCode=" + corporationCode +
                ", carId=" + carId;
    }
}
