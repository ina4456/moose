package com.insoline.pnd.remote.packets;

import com.insoline.pnd.remote.packets.server2mdt.ResponseAckPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseCallInfoPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseCallOrderPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseCallOrderProcPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseConfigPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseMessagePacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseNoticePacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponsePeriodSendingPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseReportPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseRestPacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseServicePacket;
import com.insoline.pnd.remote.packets.server2mdt.ResponseWaitCallInfoPacket;
import com.insoline.pnd.utils.LogHelper;

/**
 * Created by zic325 on 2016. 9. 7..
 */
public class ResponsePacket {

    protected int messageType;
    protected byte[] buffers;
    protected int offset;

    public ResponsePacket(byte[] bytes) {
        this.offset = 0;
        parse(bytes);
    }

    public static ResponsePacket create(int messageType, byte[] bytes) {
        switch (messageType) {
            case Packets.RESPONSE_SERVICE:  // 서비스요청 (인증요청) 결과
                return new ResponseServicePacket(bytes);

            case Packets.RESPONSE_PERIOD_SENDING:   // 주기응답
                return new ResponsePeriodSendingPacket(bytes);

            case Packets.RESPONSE_CONFIG:   // 환경설정응답
                return new ResponseConfigPacket(bytes);

            case Packets.RESPONSE_REST:     // 휴식/운행응답
                return new ResponseRestPacket(bytes);

            case Packets.RESPONSE_ACK:  // ACK 응답
                return new ResponseAckPacket(bytes);

            case Packets.RESPONSE_CALL_INFO:    // 배차고객정보 고객정보재전송 (목적지 추가)
                return new ResponseCallInfoPacket(bytes);

            case Packets.RESPONSE_CALL_ORDER: // 배차데이터 (목적지 추가)
                return new ResponseCallOrderPacket(bytes);
            case Packets.RESPONSE_CALL_ORDER_PROC:    // 배차데이터 처리 응답
                return new ResponseCallOrderProcPacket(bytes);

            case Packets.RESPONSE_NOTICE:   // 공지사항 응답
                return new ResponseNoticePacket(bytes);
            case Packets.RESPONSE_MESSAGE:  // 메시지 응답
                return new ResponseMessagePacket(bytes);

            case Packets.RESPONSE_REPORT:
                return new ResponseReportPacket(bytes);


//            case Packets.RESPONSE_ACCOUNT:
//                return new ResponseAccountPacket(bytes);

//            case Packets.RESPONSE_WAIT_CALL_INFO: // 대기배차고객정보
//                return new ResponseWaitCallInfoPacket(bytes);



//            case Packets.WAIT_PLACE_INFO:
//                return new WaitPlaceInfoPacket(bytes);
//            case Packets.RESPONSE_WAIT_DECISION:
//                return new ResponseWaitDecisionPacket(bytes);
//            case Packets.RESPONSE_WAIT_CANCEL:
//                return new ResponseWaitCancelPacket(bytes);
//            case Packets.RESPONSE_WAIT_AREA_STATE:
//                return new ResponseWaitAreaStatePacket(bytes);

//            case Packets.CANCEL_EMERGENCY:
//                return new CancelEmergencyPacket(bytes);

            default:
                return new ResponsePacket(bytes);
        }
    }

    public void parse(byte[] buffers) {
        this.buffers = buffers;
        messageType = readInt(2);
    }

    public int getMessageType() {
        return messageType;
    }

    // Little Endian
    public int readInt(int length) {
        if (isValid(length)) {
            int ret = 0;
            switch (length) {
                case 1:
                    ret = buffers[offset] & 0x000000FF;
                    break;
                case 2:
                    ret = (buffers[offset + 1] & 0x000000FF) << 8;
                    ret += (buffers[offset] & 0x000000FF) << 0;
                    break;
                case 3:
                    ret = (buffers[offset + 2] & 0x000000FF) << 16;
                    ret += (buffers[offset + 1] & 0x000000FF) << 8;
                    ret += (buffers[offset] & 0x000000FF) << 0;
                    break;
                case 4:
                    ret = (buffers[offset + 3] & 0x000000FF) << 24;
                    ret += (buffers[offset + 2] & 0x000000FF) << 16;
                    ret += (buffers[offset + 1] & 0x000000FF) << 8;
                    ret += (buffers[offset] & 0x000000FF) << 0;
                    break;
            }

            offset += length;
            return ret;
        } else {
            LogHelper.d(">> ResponsePacket.readInt() ERROR -> buffer:"
                    + ((buffers == null) ? "null" : buffers.length)
                    + " offset:" + offset + " length:" + length);
            return 0;
        }
    }

    public float readFloat(int length) {
        int bits = readInt(length);
        return Float.intBitsToFloat(bits);
    }

    public String readString(int length) {
        if (isValid(length)) {
            String str = "Encoding Fail(EUC-KR)";
            try {
                str = new String(buffers, offset, length, "EUC-KR");
            } catch (Exception e) {
                e.printStackTrace();
            }
            offset += length;

            // byte 중에 쓰레기값이 들어오는 케이스가 있어 예외처리 추가 한다.
            char szGarbage = 0xFFFD;
            str = str.replace('\r', ' ');
            str = str.replace(szGarbage, ' ');

            return str.trim();
        } else {
            LogHelper.d(">> ResponsePacket.readString() ERROR -> buffer:"
                    + ((buffers == null) ? "null" : buffers.length)
                    + " offset:" + offset + " length:" + length);
            return "";
        }
    }

    private boolean isValid(int len) {
        if (buffers == null || offset + len > buffers.length) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return "ResponsePacket{" +
                "messageType=0x" + Integer.toHexString(messageType) +
                '}';
    }
}
