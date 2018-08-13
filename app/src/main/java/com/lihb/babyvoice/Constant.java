package com.lihb.babyvoice;

/**
 * Created by lhb on 2017/2/7.
 */

public class Constant {

    public static final byte SYSNET_WIFI = 0;
    public static final byte SYSNET_MOBILE = 1;
    public static final byte SYSNET_DISCONNECT = 2;
    public static final byte SYSNET_2G = 3;
    public static final byte SYSNET_3G = 4;
    public static final byte SYSNET_4G = 5;
    public static final byte SYSNET_UNKNOWN = 127;

    // TELPHONE NETWORK TYPE
    public static final int TELNET_UNKNOWN = 0;
    public static final int TELNET_GPRS = 1;
    public static final int TELNET_EDGE = 2;
    public static final int TELNET_UMTS = 3;
    public static final int TELNET_CDMA = 4;
    public static final int TELNET_EVDO_0 = 5;
    public static final int TELNET_EVDO_A = 6;
    public static final int TELNET_1xRTT = 7;
    public static final int TELNET_HSDPA = 8;
    public static final int TELNET_HSUPA = 9;
    public static final int TELNET_HSPA = 10;
    public static final int TELNET_IDEN = 11;
    public static final int TELNET_EVDO_B = 12;
    public static final int TELNET_LTE = 13;
    public static final int TELNET_EHRPD = 14;
    public static final int TELNET_HSPAP = 15;

    // 地址
    // 用户协议
    public static final String USER_AGREEMENT = "https://www.itingbaby.com/mobile/device/agreement.do";
    // 我的关联设备
    public static final String MY_DEVICE_LIST = "https://www.itingbaby.com/mobile/device/mydevicelist.do";
    // 我的服务卡
    public static final String MY_CARD_INFO = "https://www.itingbaby.com/mobile/device/mycardinfo.do";

    public static final String ITING_MUSIC = "http://www.itingbaby.com/web/article/itemList.do?parentcode=1800";

    public static final String DATA_DIRECTORY = BabyVoiceApp.getInstance().getCachePath() + "babyVoiceRecord/";


    public static final String BlUETOOTH_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";

    public static final String BlUETOOTH_WRITE_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";

    public static final String BlUETOOTH_NOTIFY_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";

}
