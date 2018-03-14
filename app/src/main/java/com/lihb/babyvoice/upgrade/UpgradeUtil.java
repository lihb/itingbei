package com.lihb.babyvoice.upgrade;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.allenliu.versionchecklib.AVersionService;
import com.allenliu.versionchecklib.HttpRequestMethod;
import com.allenliu.versionchecklib.VersionParams;
import com.orhanobut.logger.Logger;

/**
 * Created by lihb on 2017/6/28.
 */

public class UpgradeUtil {

    private static final String UPGRADE_URL = "https://www.itingbaby.com/mobile/app/update.do";
    public static final int FROM_MAIN_ACTIVITY = 1;
    public static final int FROM_ME_FRAGMENT = 2;
    public static final String FROM = "from";

    public static void checkUpgrade(Context context, int from) {
        VersionParams versionParams = null;
        context.stopService(new Intent(context, UpgradeService.class));

        versionParams = new VersionParams()
                .setRequestUrl(UPGRADE_URL)
                .setRequestMethod(HttpRequestMethod.GET);

        Intent intent = new Intent(context, UpgradeService.class);
        intent.putExtra(AVersionService.VERSION_PARAMS_KEY, versionParams);
        intent.putExtra(FROM, from);
        context.startService(intent);
    }

    /*
     * 获取当前程序的版本号
    */
    public static String getVersionName(Context context) {
        //获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "0.0";
        }
    }

    /**
     * 版本号比较
     *
     * @param versionLocal
     * @param versionServer
     * @return
     */
    public static int compareVersion(String versionLocal, String versionServer) {
        if (versionLocal == null || versionServer == null || versionLocal.equals(versionServer)) {
            return 0;
        }
        String[] versionLocalArray = versionLocal.split("\\.");
        String[] versionServerArray = versionServer.split("\\.");
        Logger.d("UpgradeUtil", "versionLocalArray==" + versionLocalArray.length);
        Logger.d("UpgradeUtil", "versionServerArray==" + versionServerArray.length);
        int index = 0;
        // 获取最小长度值
        int minLen = Math.min(versionLocalArray.length, versionServerArray.length);
        int diff = 0;
        // 循环判断每位的大小
        Logger.d("UpgradeUtil", "verTag2=2222=" + versionLocalArray[index]);
        while (index < minLen
                && (diff = Integer.parseInt(versionLocalArray[index])
                - Integer.parseInt(versionServerArray[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            // 如果位数不一致，比较多余位数
            for (int i = index; i < versionLocalArray.length; i++) {
                if (Integer.parseInt(versionLocalArray[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < versionServerArray.length; i++) {
                if (Integer.parseInt(versionServerArray[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }


}
