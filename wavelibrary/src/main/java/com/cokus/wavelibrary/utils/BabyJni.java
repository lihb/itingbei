package com.cokus.wavelibrary.utils;

import android.util.Log;

/**
 * Created by lihb on 2018/6/2.
 */

public class BabyJni {

    public static boolean hasInitSuccess;


    static {
        try {
            System.loadLibrary("FHRAndroidLib");
            Log.e("[lihb data]", "加载so库成功");
            hasInitSuccess = true;
        } catch (UnsatisfiedLinkError var1) {
            hasInitSuccess = false;
            Log.e("[lihb data]", var1.toString());
        }
    }

    public static native int fun(int appData);

    public static native double[] FHRCal(double a1, double a2, double b1, double b2, double[] data);

}
