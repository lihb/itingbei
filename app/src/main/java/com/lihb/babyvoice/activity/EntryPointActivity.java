package com.lihb.babyvoice.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.lihb.babyvoice.BabyVoiceApp;
import com.lihb.babyvoice.customview.base.BaseFragmentActivity;
import com.lihb.babyvoice.model.UserInfo;
import com.lihb.babyvoice.utils.GsonHelper;
import com.lihb.babyvoice.utils.SharedPreferencesUtil;
import com.orhanobut.logger.Logger;


/**
 * Created by lihb on 2018/2/3.
 */

public class EntryPointActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread("EntryPointActivity") {
            @Override
            public void run() {
                try {
                    sleep(3000);
                } catch (Exception e) {
                    Logger.d("sleep error.");
                }
                if (!SharedPreferencesUtil.isFirstLaunch(EntryPointActivity.this)) {
                    SharedPreferences sharedPreferences = getSharedPreferences("userinfo", MODE_PRIVATE);

                    BabyVoiceApp.mUserInfo = GsonHelper.jsonToObject(sharedPreferences.getString("user", ""), UserInfo.class);

                    Intent intent = new Intent(EntryPointActivity.this, NewMainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(EntryPointActivity.this, StartupActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }.start();

    }
}
