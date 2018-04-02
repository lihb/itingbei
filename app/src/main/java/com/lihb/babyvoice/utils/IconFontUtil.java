package com.lihb.babyvoice.utils;

import android.graphics.Typeface;

import com.lihb.babyvoice.BabyVoiceApp;

/**
 * Created by Administrator on 2017/11/24.
 */

public class IconFontUtil {
    private static Typeface mIconfont;

    public static Typeface getIconfont() {
        if (mIconfont == null) {
            mIconfont = Typeface
                    .createFromAsset(BabyVoiceApp.getInstance().getAssets(), "iconfont/iconfont.ttf");
        }
        return mIconfont;
    }
}
