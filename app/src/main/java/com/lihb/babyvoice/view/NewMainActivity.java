package com.lihb.babyvoice.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lihb.babyvoice.BabyVoiceApp;
import com.lihb.babyvoice.Constant;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.command.BaseAndroidCommand;
import com.lihb.babyvoice.command.NetStateChangedCommand;
import com.lihb.babyvoice.command.PickedCategoryCommand;
import com.lihb.babyvoice.customview.base.BaseFragmentActivity;
import com.lihb.babyvoice.db.impl.BirthdayDataImpl;
import com.lihb.babyvoice.db.impl.PregnantDateDataImpl;
import com.lihb.babyvoice.db.impl.PregnantRemindDataImpl;
import com.lihb.babyvoice.db.impl.VaccineRemindDataImpl;
import com.lihb.babyvoice.model.BabyBirthDay;
import com.lihb.babyvoice.model.DrawLayoutEntity;
import com.lihb.babyvoice.model.PregnantDay;
import com.lihb.babyvoice.model.PregnantRemindInfo;
import com.lihb.babyvoice.model.VaccineRemindInfo;
import com.lihb.babyvoice.model.base.BaseRemindInfo;
import com.lihb.babyvoice.utils.NetworkHelper;
import com.lihb.babyvoice.utils.RecorderHelper;
import com.lihb.babyvoice.utils.RxBus;
import com.lihb.babyvoice.utils.StringUtils;
import com.lihb.babyvoice.utils.camera.PhotoHelper;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import cn.sharesdk.framework.ShareSDK;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class NewMainActivity extends BaseFragmentActivity {

    private static final String TAG = "NewMainActivity";

    private static final int HEART_TAB = 0;
    private static final int MUSIC_TAB = 1;
    private static final int ME_TAB = 2;
//    @BindView(R.id.main_activity_draw_layout)
//    DrawerLayout mainDrawLayout;
//    @BindView(R.id.drawable_layout_user_avatar)
//    CircularImageView drawableLayoutUserAvatar;
//    @BindView(R.id.drawable_layout_user_name)
//    TextView drawableLayoutUserName;
//    @BindView(R.id.drawable_layout_section_1)
//    RelativeLayout drawableLayoutSection1;
    //    @BindView(R.id.drawable_layout_achievement_num)
//    TextView drawableLayoutAchievementNum;
//    @BindView(R.id.drawable_layout_section_2)
//    RelativeLayout drawableLayoutSection2;
//    @BindView(R.id.drawable_layout_recycler_view)
//    BaseRecyclerView drawableLayoutRecyclerView;
//    @BindView(R.id.drawable_layout_setting)
//    TextView drawableLayoutSetting;
//    @BindView(R.id.drawable_layout_user_guide)
//    TextView drawableLayoutUserGuide;
//    @BindView(R.id.drawable_layout_section_3)
//    RelativeLayout drawableLayoutSection3;

    private Fragment[] mFragmentList;

    private MeFragmentNew meFragmentNew;
    private WebViewFragment mWebViewFragment;
    private HeartFragment mHeartFragment;

    //ui
    private TextView mHeartPageTab;
    private TextView mMusicTab;
    private TextView mMeTab;
    private RelativeLayout mNetErrorNoticeBar;

    private int mCurrTab = HEART_TAB;

    //    private DrawLayoutAdapter mDrawLayoutAdapter;
    private List<DrawLayoutEntity> mData = new ArrayList<>();
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        ButterKnife.bind(this);
        ShareSDK.initSDK(this);

        initViews();
        checkNetStatus();
//        UpgradeUtil.checkUpgrade(NewMainActivity.this, UpgradeUtil.FROM_MAIN_ACTIVITY);
        checkVaccineRemind();
        checkPregnantExamineRemind();
//        CommonToast.showShortToast(UpgradeUtil.getVersionName(NewMainActivity.this));
    }

    private void checkVaccineRemind() {
        BirthdayDataImpl.getInstance()
                .queryAllData()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(babyBirthDays -> {
                    if (!babyBirthDays.isEmpty()) {
                        BabyBirthDay birthDay = babyBirthDays.get(babyBirthDays.size() - 1);
                        String date = birthDay.birthday;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                        Date birthday;
                        int age = 0;
                        try {
                            birthday = sdf.parse(date);
                            age = StringUtils.differentDays(birthday, new Date());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        int finalAge = age;
                        VaccineRemindDataImpl.getInstance()
                                .queryAllData()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(vaccineRemindList -> {
                                    for (VaccineRemindInfo info : vaccineRemindList) {
                                        if (info.ageToInject * 30 > finalAge && info.hasRead == 0) {
                                            showRemindDialog(NewMainActivity.this, info);
                                            break;
                                        }
                                    }
                                });
                    }

                });
    }

    private void checkPregnantExamineRemind() {
        PregnantDateDataImpl.getInstance()
                .queryAllData()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(pregnantDayList -> {
                    if (!pregnantDayList.isEmpty()) {
                        PregnantDay pregnantDay = pregnantDayList.get(pregnantDayList.size() - 1);
                        String date = pregnantDay.pregnantDay;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                        Date birthday;
                        int age = 0;
                        try {
                            birthday = sdf.parse(date);
                            age = StringUtils.differentDays(birthday, new Date());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        int finalAge = age;
                        PregnantRemindDataImpl.getInstance()
                                .queryAllData()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(pregnantRemindInfoList -> {
                                    for (PregnantRemindInfo info : pregnantRemindInfoList) {
                                        if (info.eventDate * 30 > finalAge && info.hasRead == 0) {
                                            showRemindDialog(NewMainActivity.this, info);
                                            break;
                                        }
                                    }
                                });
                    }

                });

    }

    private void showRemindDialog(final Activity activity, BaseRemindInfo remindInfo) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (remindInfo instanceof VaccineRemindInfo) {
            builder.setTitle(R.string.vaccine_remind_txt);
            if (StringUtils.getSystemLanguage(activity).endsWith("zh")) {
                builder.setMessage(((VaccineRemindInfo) remindInfo).vaccineName);
            } else {
                builder.setMessage(((VaccineRemindInfo) remindInfo).vaccineNameEn);
            }
        } else {
            builder.setTitle(R.string.pregnant_check_remind_txt);
            if (StringUtils.getSystemLanguage(activity).endsWith("zh")) {
                builder.setMessage(((PregnantRemindInfo) remindInfo).eventName);
            } else {
                builder.setMessage(((PregnantRemindInfo) remindInfo).eventNameEn);
            }
        }
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.got_it, (dialog, which) -> {
            if (remindInfo instanceof VaccineRemindInfo) {
                VaccineRemindInfo info = (VaccineRemindInfo) remindInfo;
                info.hasRead = 1;
                VaccineRemindDataImpl.getInstance()
                        .updateData(info)
                        .subscribe(b -> {
                            Logger.i(info.toString() + "-疫苗更新为已读成功。");
                        });
            } else {
                PregnantRemindInfo info = (PregnantRemindInfo) remindInfo;
                info.hasRead = 1;
                PregnantRemindDataImpl.getInstance()
                        .updateData(info)
                        .subscribe(b -> {
                            Logger.i(info.toString() + "-疫苗更新为已读成功。");
                        });
            }
        });
        if (!activity.isFinishing()) {
            builder.show();
        }

    }

    private void initViews() {
        mHeartPageTab = (TextView) findViewById(R.id.heart_textview);
        mMusicTab = (TextView) findViewById(R.id.music_textview);
        mMeTab = (TextView) findViewById(R.id.me_textview);
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        mHeartPageTab.setOnClickListener(mTabOnClickListener);
        mMusicTab.setOnClickListener(mTabOnClickListener);
        mMeTab.setOnClickListener(mTabOnClickListener);

//        initDrawableLayoutTopView();

//        drawableLayoutSection1.setOnClickListener(v -> {
//            showDrawableLayout(false);
//            switchToPersonalInfoFragment();
//        });
//        drawableLayoutSetting.setOnClickListener(v -> {
//            showDrawableLayout(false);
//            switchToSettingFragment();
//        });

//        drawableLayoutUserGuide.setOnClickListener(v -> {
//            showDrawableLayout(false);
//            WebViewActivity.navigate(NewMainActivity.this, Constant.USER_AGREEMENT, null);
//        });

        mNetErrorNoticeBar = (RelativeLayout) findViewById(R.id.net_error_notice_bar);

        mHeartFragment = HeartFragment.create();
        mWebViewFragment = WebViewFragment.create();
        meFragmentNew = MeFragmentNew.create();

        mFragmentList = new Fragment[]{mHeartFragment, mWebViewFragment, meFragmentNew};

        // 加入fragment,显示爱听贝tab
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_layout, mHeartFragment)
                .add(R.id.main_layout, meFragmentNew)
                .add(R.id.main_layout, mWebViewFragment)
                .show(mHeartFragment)
                .hide(meFragmentNew)
                .hide(mWebViewFragment)
                .commit();

        switchToFragment(HEART_TAB);

        initDrawLayoutData();
//        mDrawLayoutAdapter = new DrawLayoutAdapter(getContext(), mData);
//        drawableLayoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        drawableLayoutRecyclerView.setAdapter(mDrawLayoutAdapter);
    }

    private void initDrawLayoutData() {
        for (int i = 0; i < 4; i++) {
            DrawLayoutEntity entity = new DrawLayoutEntity();
            entity.itemUrl = "";
            mData.add(entity);
        }
//        mData.get(0).title = "我的钱包";
//        mData.get(0).detail = "0元";

        mData.get(0).title = "我的卡券";
        mData.get(0).icon = getString(R.string.my_card);
//        mData.get(2).title = "用户协议";
//        mData.get(2).itemUrl = Constant.USER_AGREEMENT;
        mData.get(1).title = "我的设备";
        mData.get(1).itemUrl = Constant.MY_DEVICE_LIST;
        mData.get(1).icon = getString(R.string.my_devices);

        mData.get(2).title = "分享";
        mData.get(2).icon = getString(R.string.share_friends);

        mData.get(3).title = "蓝牙";
        mData.get(3).icon = getString(R.string.my_devices);


//        mData.get(3).itemUrl = Constant.MY_CARD_INFO;

    }

    private View.OnClickListener mTabOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            int index;
            if (view == mHeartPageTab) {
                index = HEART_TAB;
            } else if (view == mMusicTab) {
                index = MUSIC_TAB;
            } else {
                index = ME_TAB;
            }
            if (index != mCurrTab) {
                switchToFragment(index);
            }

        }
    };

    /**
     * 跳转到指定的fragment
     *
     * @param index 需要跳转fragment的index
     */
    private void switchToFragment(int index) {
        Logger.i("switchToView() index = %d", index);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        transaction.hide(mFragmentList[mCurrTab])
                .show(mFragmentList[index])
                .commit();
        changeIndicatorByIndex(index);
    }

    /**
     * 改变底部tab图片和文字颜色
     *
     * @param index 选中的tab的index
     */
    private void changeIndicatorByIndex(int index) {
        switch (index) {
            case HEART_TAB:
                mHeartPageTab.setSelected(true);
                mMeTab.setSelected(false);
                mMusicTab.setSelected(false);
                mCurrTab = HEART_TAB;
                break;
            case MUSIC_TAB:
                mHeartPageTab.setSelected(false);
                mMeTab.setSelected(false);
                mMusicTab.setSelected(true);
                mCurrTab = MUSIC_TAB;
                break;
            case ME_TAB:
                mHeartPageTab.setSelected(false);
                mMeTab.setSelected(true);
                mMusicTab.setSelected(false);
                mCurrTab = ME_TAB;
                break;
            default:
                break;
        }
    }


    private PersonalInfoFragment mPersonalInfoFragment;

    private void switchToPersonalInfoFragment() {
        if (null == mPersonalInfoFragment) {
            mPersonalInfoFragment = PersonalInfoFragment.create();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(mFragmentList[mCurrTab]);
        transaction.replace(R.id.main_layout, mPersonalInfoFragment, "PersonalInfoFragment")
                .show(mPersonalInfoFragment)
                .addToBackStack(null)
                .commit();


    }

    private void switchToMeFragment() {
        if (null == meFragmentNew) {
            meFragmentNew = MeFragmentNew.create();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(mFragmentList[mCurrTab]);
        transaction.replace(R.id.main_layout, meFragmentNew, "MeFragmentNew")
                .show(meFragmentNew)
                .addToBackStack(null)
                .commit();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RecorderHelper.getInstance().cancel();
    }

    private void checkNetStatus() {
        updateNetErrorNoticeBar();
        RxBus.getDefault().registerOnActivity(BaseAndroidCommand.class, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BaseAndroidCommand>() {
                    @Override
                    public void call(BaseAndroidCommand baseAndroidCommand) {
                        if (baseAndroidCommand instanceof NetStateChangedCommand) {
                            updateNetErrorNoticeBar();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.d("BaseAndroidCommand failed.", throwable);
                    }
                });
        RxBus.getDefault().registerOnActivity(PickedCategoryCommand.class, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PickedCategoryCommand>() {
                    @Override
                    public void call(PickedCategoryCommand pickedCategoryCommand) {
                        int type = pickedCategoryCommand.getAction();
                        Logger.i("type is %d", type);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.e(throwable, "error");
                    }
                });
    }

    private void updateNetErrorNoticeBar() {
        mNetErrorNoticeBar.setVisibility(NetworkHelper.isDisconnected(NewMainActivity.this) ? View.VISIBLE : View.GONE);
    }

    private void addStatusBarView() {
        View view = new View(this);
        view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(this));
        ViewGroup decorView = (ViewGroup) findViewById(android.R.id.content);
        decorView.addView(view, params);
    }

    public int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

//    public void showDrawableLayout(boolean enableShow) {
//        if (enableShow) {
//            initDrawableLayoutTopView();
//            mainDrawLayout.openDrawer(Gravity.LEFT);
//        } else {
//            mainDrawLayout.closeDrawers();
//        }
//    }

//    public void toggleDrawableLayout(boolean open) {
//        mainDrawLayout.setDrawerLockMode(open ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoHelper.REQUEST_TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            String picturePath = data.getStringExtra(PhotoHelper.OUTPUT_PATH);
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("PersonalInfoFragment");
            if (fragment != null && fragment instanceof PersonalInfoFragment && fragment.isVisible()) {
                ((PersonalInfoFragment) fragment).updatePhoto(picturePath);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getCanExit()) {
            BabyVoiceApp.getInstance().ExitAPP(getApplicationContext());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 对不同的fragment区分不同的退出机制
     *
     * @return
     */
    private boolean getCanExit() {
        if (getSupportFragmentManager().findFragmentByTag("SettingFragment") != null &&
                getSupportFragmentManager().findFragmentByTag("SettingFragment").isVisible()) {
            return false;
        }
        if (getSupportFragmentManager().findFragmentByTag("msgFragment") != null &&
                getSupportFragmentManager().findFragmentByTag("msgFragment").isVisible()) {
            return false;
        } else {
            return true;
        }
    }
}
