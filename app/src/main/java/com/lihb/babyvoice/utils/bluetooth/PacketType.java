package com.lihb.babyvoice.utils.bluetooth;

/**
 * Created by lihb on 2018/6/2.
 */

public interface PacketType {

    /**
     * 握手命令
     */
    byte COMMAND_HAND_CHECK = (byte) 0x10;

    /**
     * 胎心音数据
     */
    byte COMMAND_BABY_VOICE_DATA = (byte) 0x20;

    /**
     * 文件管理
     */
    byte COMMAND_FILE_MANAGER = (byte) 0x80;

    /**
     * 在线升级
     */
    byte COMMAND_UPGRADE_ONLINE = (byte) 0x90;

    /**
     * 数据包错误
     */
    byte COMMAND_PACKET_ERROR = (byte) 0xFF;


}
