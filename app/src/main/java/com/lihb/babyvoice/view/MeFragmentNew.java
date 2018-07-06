package com.lihb.babyvoice.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lihb.babyvoice.R;
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

    private TitleBar mTitleBar;
    private PersonalInfoFragment mPersonalInfoFragment;

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

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

}
