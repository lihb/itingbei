package com.lihb.babyvoice.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.customview.CommonItem;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.upgrade.UpgradeUtil;
import com.lihb.babyvoice.utils.CommonToast;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by lhb on 2017/2/8.
 */

public class SettingFragment extends BaseFragment {

    public static final int ITEM_SET_PREGNANT_DATE = 100;
    public static final int ITEM_SET_BABY_BIRTHDAY = 200;
    private static final String TAG = "SettingFragment";

    private CommonItem itemMeCenter;
    private CommonItem itemPersonalInfo;
    private CommonItem itemBabyInfo;
    //    private CommonItem itemRemoteVideoAddress;
    private CommonItem itemLanguageSelect;
    private CommonItem itemAboutApp;
    private CommonItem itemVersionUpdate;
    private DateSelectFragment mDateSelectFragment;
    private PersonalInfoFragment mPersonalInfoFragment;
    private TitleBar mTitleBar;
    private HeartFragment mHeartFragment;
    private CommonItem itemScan;

    public static SettingFragment create() {
        return new SettingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        itemVersionUpdate = (CommonItem) getView().findViewById(R.id.item_version_update);
        itemVersionUpdate.setOnClickListener(v -> UpgradeUtil.checkUpgrade(getActivity(), UpgradeUtil.FROM_ME_FRAGMENT));

        mTitleBar = (TitleBar) getView().findViewById(R.id.title_bar);
        mTitleBar.setLeftOnClickListener(v -> gotoHeartFragment());

        itemAboutApp = (CommonItem) getView().findViewById(R.id.item_about_app);
        itemAboutApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonToast.showShortToast("itemAboutApp");
            }
        });

        itemScan = (CommonItem) getView().findViewById(R.id.item_scan);
        itemScan.setOnClickListener(v -> {
            toScanBarCode();
        });


    }

    private void toScanBarCode() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setPrompt("请扫描"); //底部的提示文字，设为""可以置空
        integrator.setCameraId(0); //前置或者后置摄像头
        integrator.setBeepEnabled(false); //扫描成功的「哔哔」声，默认开启
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.initiateScan();
    }

    // Get the results:
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    private void gotoDateSelectFragment(int type) {
        if (null == mDateSelectFragment) {
            mDateSelectFragment = DateSelectFragment.create();
        }
        Bundle bundle = new Bundle();
        bundle.putInt("itemType", type);
        mDateSelectFragment.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this);
        transaction.add(R.id.main_layout, mDateSelectFragment, "DateSelectFragment")
                .show(mDateSelectFragment)
                .addToBackStack(null)
                .commit();

    }

    private void gotoHeartFragment() {
        if (null == mHeartFragment) {
            mHeartFragment = HeartFragment.create();
        }

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this);
        int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        }
        transaction.replace(R.id.main_layout, mHeartFragment, "HeartFragment")
                .show(mHeartFragment)
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
