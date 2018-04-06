package com.lihb.babyvoice.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 *
 */
public class PermissionCheckUtil {

    public static final int REQUEST_PERMISSION = 10087;

    public static boolean checkHasPermission(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }


    public static void showGrantFailDialog(final Activity activity) {
        //权限申请失败
        showGrantFailDialog(activity, "您关闭了访问存储空间的权限！去手机设置中修改吧~");
    }

    public static void showGrantFailDialog(final Activity activity, String msg) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(activity);
//        builder.setTitle("Material Design Dialog");
        builder.setMessage(msg);
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("去设置", (dialog, which) ->
                RuntimePermissionsUtil.startInstalledAppDetailsActivity(activity));
        if (!activity.isFinishing()) {
            builder.show();
        }

    }


    /**
     * 检查是否拥有指定的所有权限
     */
    public static boolean checkPermissionAllGranted(Activity activity, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    /**
     * 请求权限
     */
    public static void requestPermission(Activity activity, String[] permissions) {
        ActivityCompat.requestPermissions(
                activity,
                permissions,
                REQUEST_PERMISSION
        );
    }

}
