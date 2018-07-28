package com.lihb.babyvoice.utils.bluetooth;

import android.util.Log;

import com.clj.fastble.utils.HexUtil;
import com.lihb.babyvoice.command.BluetoothCommand;
import com.lihb.babyvoice.utils.RxBus;
import com.lihb.babyvoice.utils.StringUtils;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by lihb on 2018/5/24.
 */

public class BluetoothParser {

    public static final String TAG = BluetoothParser.class.getSimpleName();

    public static final String HAND_SIGNAL = "AA0B10010000000000001C55";

    public static final String HEART_SIGNAL = "AA0610030A2355";

    /**
     * 包头
     */
    private byte packetHead = (byte) 0xAA;

    /**
     * 长度, 1 byte
     */
    private byte packetLen;

    /**
     * 数据类型
     */

    private byte packetType;

    /**
     * 数据含义
     */

    private byte packetSubType;

    /**
     * 数据内容
     */

    private byte[] packetContent;

    /**
     * 校验和
     */

    private byte packetCheckSum;  // 怎么校验

    /**
     * 包尾
     */

    private byte packetTail = 0x55;

    private LinkedBlockingQueue<Byte> inputBytesQueue = new LinkedBlockingQueue<Byte>();

    private BluetoothParser() {
    }

    private static class Holder {
        private static final BluetoothParser INSTANCE = new BluetoothParser();
    }

    public static BluetoothParser getInstance() {
        return Holder.INSTANCE;
    }

    public void putBytes(byte data) {
        try {
            inputBytesQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void putBytes(byte[] dataArr) {
        try {
            for (int i = 0; i < dataArr.length; i++) {
                inputBytesQueue.put(dataArr[i]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private byte takeByte() {
        try {
            return inputBytesQueue.take();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (byte) 0xff;
    }

    public void parserBytes(Byte[] data) {
//        byte[] data = takeBytes();
        Log.d(TAG, HexUtil.formatHexString(StringUtils.toPrimitives(data), true));
        try {
            if (data != null) {
                packetHead = data[0];
                packetLen = data[1];
                int contentLength = packetLen - 4; // FIXME: 2018/7/29
                Log.d(TAG, "packetLen = " + packetLen + ", contentLength = " + contentLength);
                packetType = data[2];
                packetSubType = data[3];
                packetContent = new byte[contentLength];
                System.arraycopy(StringUtils.toPrimitives(data), 4, packetContent, 0, contentLength);
                packetCheckSum = data[packetLen - 1];
                packetTail = data[packetLen];
                BluetoothCommand.BlueToothStatus status = null;

                if (packetHead == (byte) 0xAA) { // 命令包
                    if (packetType == PacketType.COMMAND_HAND_CHECK) {
                        switch (packetSubType) {
                            case 0x01:
                                status = BluetoothCommand.BlueToothStatus.PHONE_SEND_HAND_SIGNAL;
                                break;

                            case 0x02:
                                status = BluetoothCommand.BlueToothStatus.DEV_REPLY_HAND_SIGNAL;
                                break;

                            case 0x03:
                                status = BluetoothCommand.BlueToothStatus.HEART_BEAT_SIGNAL;
                                break;

                            case 0x04:
                                status = BluetoothCommand.BlueToothStatus.PHONE_STOP_SIGNAL;
                                break;

                        }
                    } else if (packetType == PacketType.COMMAND_BABY_VOICE_DATA) {
                        switch (packetSubType) {
                            case 0x01:
                                status = BluetoothCommand.BlueToothStatus.DEV_UPLOAD_VOICE_DATA_SIGNAL;
                                break;

                            case 0x02:
                                status = BluetoothCommand.BlueToothStatus.PHONE_SETTING_SIGNAL;
                                break;

                            case 0x03:
                                status = BluetoothCommand.BlueToothStatus.DEV_UPLOAD_STATUS_SIGNAL;
                                break;

                            case 0x04:
                                status = BluetoothCommand.BlueToothStatus.DEV_UPLOAD_BATTERY_LEFT_SIGNAL;
                                break;

                        }

                    } else if (packetType == PacketType.COMMAND_FILE_MANAGER) {
                        switch (packetSubType) {
                            case 0x01:
                                break;
                        }

                    } else if (packetType == PacketType.COMMAND_UPGRADE_ONLINE) {
                        switch (packetSubType) {
                            case 0x01:
                                break;
                        }

                    } else if (packetType == PacketType.COMMAND_PACKET_ERROR) {
                        switch (packetSubType) {
                            case 0x01:
                                status = BluetoothCommand.BlueToothStatus.DEV_PACKET_ERROR_SIGNAL;
                                break;
                        }
                    }
                    if (status != null) {
                        BluetoothCommand command = new BluetoothCommand(status, packetContent);
                        RxBus.getDefault().post(command);
                    }
                }
//            data = takeBytes();

            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

    }


}
