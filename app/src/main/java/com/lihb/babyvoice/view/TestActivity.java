package com.lihb.babyvoice.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.activity.StartupActivity;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragmentActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lihb on 2018/7/5.
 */

public class TestActivity extends BaseFragmentActivity {


    private TitleBar mTitleBar;

    private TextSwitcher mTextSwitcher = null;

    private LinkedBlockingQueue<String> data = new LinkedBlockingQueue<>();
    private int count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();

    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(TestActivity.this, StartupActivity.class);
        startActivity(intent);
        finish();
    }

    private void initView() {

        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTextSwitcher = (TextSwitcher) findViewById(R.id.text_switcher);
        initData();

        mTextSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.bulletin_item_enter));
        mTextSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.bulletin_item_leave));
        mTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView view = new TextView(TestActivity.this);
                view.setTextColor(getResources().getColor(R.color.tab_txt_selected));
                view.setTextSize(20);
                view.setGravity(Gravity.CENTER_VERTICAL);
                return view;
            }
        });
        Observable observable = Observable.interval(3, TimeUnit.SECONDS).observeOn(Schedulers.io());

        Subscription subscription = observable.subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                String msg = data.poll();
                if (msg == null) {
                    msg = "新增的测试信息 " + new Random().nextInt(100);
                    count++;
                }
                Log.d("lihb 测试", "数据 = " + msg);
                String finalMsg = msg;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextSwitcher.setText(finalMsg);
                    }
                });
            }
        });

    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            data.add("测试信息 " + i);
        }
    }


    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("RegisterActivity");
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("RegisterActivity");
        MobclickAgent.onPause(this);
    }
}
