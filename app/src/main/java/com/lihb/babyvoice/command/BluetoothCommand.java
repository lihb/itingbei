package com.lihb.babyvoice.command;

/**
 * Created by lihb on 2018/5/24.
 */

public class BluetoothCommand extends BaseAndroidCommand {

    public enum BlueToothStatus {
        PHONE_SEND_HAND_SIGNAL,
        DEV_REPLY_HAND_SIGNAL,
        HEART_BEAT_SIGNAL,
        PHONE_STOP_SIGNAL,
        DEV_UPLOAD_VOICE_DATA_SIGNAL,
        PHONE_SETTING_SIGNAL,
        DEV_UPLOAD_STATUS_SIGNAL,
        DEV_UPLOAD_BATTERY_LEFT_SIGNAL,
        DEV_PACKET_ERROR_SIGNAL,

    }

    private BlueToothStatus mStatus;

    private byte[] data;

    public BluetoothCommand(BlueToothStatus status, byte[] data) {
        mStatus = status;
        this.data = data;
    }

    public BlueToothStatus getmStatus() {
        return mStatus;
    }

    public byte[] getData() {
        return data;
    }
}
