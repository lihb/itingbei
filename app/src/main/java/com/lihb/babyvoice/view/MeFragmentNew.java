package com.lihb.babyvoice.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lihb.babyvoice.BabyVoiceApp;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.action.ServiceGenerator;
import com.lihb.babyvoice.customview.CircularImageView;
import com.lihb.babyvoice.customview.CommonItem;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.utils.CommonToast;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by lhb on 2017/2/8.
 */

public class MeFragmentNew extends BaseFragment {

    private static final String TAG = "MeFragmentNew";

    private CommonItem itemWallet;
    private CommonItem itemDevices;
    private CommonItem itemExchange;
    private CommonItem itemBluetooth;

    CircularImageView drawableLayoutUserAvatar;
    TextView drawableLayoutUserName;
    RelativeLayout userInfoLayout;

    private TitleBar mTitleBar;
    private PersonalInfoFragment mPersonalInfoFragment;
    private SettingFragment mSettingFragment;

    public static MeFragmentNew create() {
        return new MeFragmentNew();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me_new, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        itemWallet = (CommonItem) getView().findViewById(R.id.item_wallet);
        itemWallet.setOnClickListener(v -> CommonToast.showShortToast("钱包"));

        mTitleBar = (TitleBar) getView().findViewById(R.id.title_bar);
        mTitleBar.setRightOnClickListener(v -> {
            gotoSettingFragment();
        });

        itemDevices = (CommonItem) getView().findViewById(R.id.item_devices);
        itemDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonToast.showShortToast("设备");
            }
        });

        itemExchange = (CommonItem) getView().findViewById(R.id.item_exchange_device);
        itemExchange.setOnClickListener(v -> {
            CommonToast.showShortToast("退换货");
        });

        itemBluetooth = (CommonItem) getView().findViewById(R.id.item_bluetooth);
        itemBluetooth.setOnClickListener(v -> {
            BluetoothActivityNew.navigate(getContext());
        });

        drawableLayoutUserAvatar = (CircularImageView) getView().findViewById(R.id.drawable_layout_user_avatar);
        drawableLayoutUserName = (TextView) getView().findViewById(R.id.drawable_layout_user_name);
        userInfoLayout = (RelativeLayout) getView().findViewById(R.id.user_info_layout);
        userInfoLayout.setOnClickListener(v -> {
            gotoPersonalInfoFragment();
        });

        initUserInfo();

    }

    private void initUserInfo() {
        if (BabyVoiceApp.mUserInfo != null) {
            String headIcon = BabyVoiceApp.mUserInfo.headicon;
            headIcon = ServiceGenerator.API_BASE_URL + headIcon;

            Glide.with(this)
                    .load(headIcon)
                    .placeholder(R.mipmap.logo)
                    .error(R.mipmap.logo)
                    .dontAnimate()
                    .into(drawableLayoutUserAvatar);

            drawableLayoutUserName.setText(TextUtils.isEmpty(BabyVoiceApp.mUserInfo.nickname) ? BabyVoiceApp.mUserInfo.realname : BabyVoiceApp.mUserInfo.nickname);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden == false) {
            showBottomTab();
        }
    }

    private void showBottomTab() {
        if (getActivity() == null) {
            return;
        }
        // 隐藏底部的导航栏和分割线
        (getActivity().findViewById(R.id.tab_layout)).setVisibility(View.VISIBLE);
        (getActivity().findViewById(R.id.main_divider_line)).setVisibility(View.VISIBLE);
    }

    private void gotoPersonalInfoFragment() {
        if (null == mPersonalInfoFragment) {
            mPersonalInfoFragment = PersonalInfoFragment.create();
        }

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this);
        transaction.add(R.id.main_layout, mPersonalInfoFragment, "PersonalInfoFragment")
                .show(mPersonalInfoFragment)
                .addToBackStack(null)
                .commit();


    }

    private void gotoSettingFragment() {
        if (null == mSettingFragment) {
            mSettingFragment = SettingFragment.create();
        }

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this);
        transaction.add(R.id.main_layout, mSettingFragment, "SettingFragment")
                .show(mSettingFragment)
                .addToBackStack(null)
                .commit();


    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

}
