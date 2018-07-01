package com.lihb.babyvoice.view;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.utils.HexUtil;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.command.BluetoothCommand;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragmentActivity;
import com.lihb.babyvoice.observer.Observer;
import com.lihb.babyvoice.observer.ObserverManager;
import com.lihb.babyvoice.utils.CommonToast;
import com.lihb.babyvoice.utils.RxBus;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class OperationActivity extends BaseFragmentActivity implements Observer {

    public static final String KEY_DATA = "key_data";

    private BleDevice bleDevice;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic characteristic;
    private int charaProp;

    private List<Fragment> fragments = new ArrayList<>();
    private int currentPage = 0;
    private String[] titles = new String[3];
    private TitleBar mTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_operation);
        initData();
        initView();
        initPage();

        ObserverManager.getInstance().addObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().clearCharacterCallback(bleDevice);
        ObserverManager.getInstance().deleteObserver(this);
    }

    @Override
    public void disConnected(BleDevice device) {
        if (device != null && bleDevice != null && device.getKey().equals(bleDevice.getKey())) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentPage != 0) {
                currentPage--;
                changePage(currentPage);
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(titles[0]);
        mTitleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage != 0) {
                    currentPage--;
                    changePage(currentPage);
                } else {
                    finish();
                }
            }
        });
        RxBus.getDefault().registerOnActivity(BluetoothCommand.class, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BluetoothCommand>() {
                    @Override
                    public void call(BluetoothCommand command) {
                        if (command.getmStatus() == BluetoothCommand.BlueToothStatus.PHONE_SEND_HAND_SIGNAL) {
                            Logger.i("主机发起握手信号");
                        } else if (command.getmStatus() == BluetoothCommand.BlueToothStatus.DEV_REPLY_HAND_SIGNAL) {
                            Logger.i("从机回复握手信号");
                        } else if (command.getmStatus() == BluetoothCommand.BlueToothStatus.HEART_BEAT_SIGNAL) {
                            Logger.i("心跳包: " + HexUtil.formatHexString(command.getData(), true));
                            CommonToast.showShortToast("收到心跳包: " + HexUtil.formatHexString(command.getData(), true));
                        } else if (command.getmStatus() == BluetoothCommand.BlueToothStatus.PHONE_STOP_SIGNAL) {
                            Logger.i("主机正常断开蓝牙前的通知数据包");
                        } else if (command.getmStatus() == BluetoothCommand.BlueToothStatus.DEV_UPLOAD_VOICE_DATA_SIGNAL) {
                            Logger.i("从机上传实时胎心音数据");
                        } else if (command.getmStatus() == BluetoothCommand.BlueToothStatus.PHONE_SETTING_SIGNAL) {
                            Logger.i("主机配置设备的信息");
                        } else if (command.getmStatus() == BluetoothCommand.BlueToothStatus.DEV_UPLOAD_STATUS_SIGNAL) {
                            Logger.i("从机主动上传设备状态信息");
                        } else if (command.getmStatus() == BluetoothCommand.BlueToothStatus.DEV_UPLOAD_BATTERY_LEFT_SIGNAL) {
                            Logger.i("从机回复电池剩余可用监护时间");
                        } else if (command.getmStatus() == BluetoothCommand.BlueToothStatus.DEV_PACKET_ERROR_SIGNAL) {
                            Logger.i("设备解析数据包时发现错误，进行信息返回。");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.e("headset out ,error: %s", throwable.getMessage());
                    }
                });

    }

    private void initData() {
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null)
            finish();

        titles = new String[]{
                getString(R.string.service_list),
                getString(R.string.characteristic_list),
                getString(R.string.console)};
    }

    private void initPage() {
        prepareFragment();
        changePage(0);
    }

    public void changePage(int page) {
        currentPage = page;
        mTitleBar.setTitle(titles[page]);

        updateFragment(page);
        if (currentPage == 1) {
            ((CharacteristicListFragment) fragments.get(1)).showData();
        } else if (currentPage == 2) {
            ((CharacteristicOperationFragment) fragments.get(2)).showData();
        }
    }

    private void prepareFragment() {
        fragments.add(new ServiceListFragment());
        fragments.add(new CharacteristicListFragment());
        fragments.add(new CharacteristicOperationFragment());
        for (Fragment fragment : fragments) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, fragment).hide(fragment).commit();
        }
    }

    private void updateFragment(int position) {
        if (position > fragments.size() - 1) {
            return;
        }
        for (int i = 0; i < fragments.size(); i++) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = fragments.get(i);
            if (i == position) {
                transaction.show(fragment);
            } else {
                transaction.hide(fragment);
            }
            transaction.commit();
        }
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public BluetoothGattService getBluetoothGattService() {
        return bluetoothGattService;
    }

    public void setBluetoothGattService(BluetoothGattService bluetoothGattService) {
        this.bluetoothGattService = bluetoothGattService;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public int getCharaProp() {
        return charaProp;
    }

    public void setCharaProp(int charaProp) {
        this.charaProp = charaProp;
    }


}
