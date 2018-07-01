package com.lihb.babyvoice.utils.bluetooth;

import android.bluetooth.BluetoothAdapter;

import java.util.UUID;

/**
 * Created by lihb on 2018/6/20.
 */

public class LbeBluetoothManager {


    /**
     * 开始BLE设备扫描
     *
     * @param serviceUuids
     * @param callback
     */
    public static void startLeScan(UUID[] serviceUuids, BluetoothAdapter.LeScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (serviceUuids == null) {
            adapter.startLeScan(callback);
        } else {
            adapter.startLeScan(serviceUuids, callback);
        }
    }


    /**
     * 停止BLE设备扫描
     *
     * @param callback
     */
    public static void stopLeScan(BluetoothAdapter.LeScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.stopLeScan(callback);
    }
}
