package com.lihb.babyvoice.utils;

import com.orhanobut.logger.Logger;

/**
 * Created by lihb on 2018/6/2.
 */

public class BabyJni {

    public static boolean hasInitSuccess;

    public BabyJni() {
        Logger.e("初始化");

    }

    static {
        try {
            System.loadLibrary("FHRAndroidLib");
            Logger.e("加载so库成功");
            hasInitSuccess = true;
        } catch (UnsatisfiedLinkError var1) {
            hasInitSuccess = false;
            Logger.e(var1.getMessage());
        }
    }


    public native double[] FHRCal(double a1, double a2, double b1, double b2, double[] data, int length);

}
