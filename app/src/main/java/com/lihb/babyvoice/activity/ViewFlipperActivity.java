package com.lihb.babyvoice.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.customview.base.BaseActivity;

/**
 * Created by lihb on 2018/9/2.
 */

public class ViewFlipperActivity extends BaseActivity implements android.view.GestureDetector.OnGestureListener {

    private int[] imgs = {R.mipmap.guide_sound_0, R.mipmap.guide_sound_1,
            R.mipmap.guide_sound_2};

    private GestureDetector gestureDetector = null;
    private ViewFlipper viewFlipper = null;
    private TextView startRightNow = null;

    private Activity mActivity = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flipper);

        mActivity = this;

        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        startRightNow = (TextView) findViewById(R.id.start_right_now);
        gestureDetector = new GestureDetector(this);    // 声明检测手势事件

        for (int i = 0; i < imgs.length; i++) {            // 添加图片源
            ImageView iv = new ImageView(this);
            iv.setImageResource(imgs[i]);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            viewFlipper.addView(iv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }

        viewFlipper.setAutoStart(false);            // 设置自动播放功能（点击事件，前自动播放）

//        viewFlipper.setFlipInterval(3000);
//        if(viewFlipper.isAutoStart() && !viewFlipper.isFlipping()){
//            viewFlipper.startFlipping();
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewFlipper.stopFlipping();                // 点击事件后，停止自动播放
        viewFlipper.setAutoStart(false);
        return gestureDetector.onTouchEvent(event);        // 注册手势事件
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e2.getX() - e1.getX() > 120) {             // 从左向右滑动（左进右出）
            Animation rInAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_right_in);    // 向右滑动左侧进入的渐变效果（alpha  0.1 -> 1.0）
            Animation rOutAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_right_out); // 向右滑动右侧滑出的渐变效果（alpha 1.0  -> 0.1）

            viewFlipper.setInAnimation(rInAnim);
            viewFlipper.setOutAnimation(rOutAnim);
            if (viewFlipper.getDisplayedChild() > 0) {
                viewFlipper.showPrevious();
            }
            if (viewFlipper.getDisplayedChild() == imgs.length - 1) {
                startRightNow.setVisibility(View.VISIBLE);
            } else {
                startRightNow.setVisibility(View.GONE);

            }
            return true;
        } else if (e2.getX() - e1.getX() < -120) {         // 从右向左滑动（右进左出）
            Animation lInAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_left_in);        // 向左滑动左侧进入的渐变效果（alpha 0.1  -> 1.0）
            Animation lOutAnim = AnimationUtils.loadAnimation(mActivity, R.anim.push_left_out);    // 向左滑动右侧滑出的渐变效果（alpha 1.0  -> 0.1）

            viewFlipper.setInAnimation(lInAnim);
            viewFlipper.setOutAnimation(lOutAnim);
            if (viewFlipper.getDisplayedChild() < imgs.length - 1) {
                viewFlipper.showNext();
            }
            if (viewFlipper.getDisplayedChild() == imgs.length - 1) {
                startRightNow.setVisibility(View.VISIBLE);
            } else {
                startRightNow.setVisibility(View.GONE);

            }
            return true;
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

}
