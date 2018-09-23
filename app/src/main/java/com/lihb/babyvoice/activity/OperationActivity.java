package com.lihb.babyvoice.activity;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.utils.HexUtil;
import com.cokus.wavelibrary.utils.Pcm2Wav;
import com.lihb.babyvoice.Constant;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.command.BluetoothCommand;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragmentActivity;
import com.lihb.babyvoice.fragment.CharacteristicOperationFragment;
import com.lihb.babyvoice.observer.Observer;
import com.lihb.babyvoice.observer.ObserverManager;
import com.lihb.babyvoice.utils.FileUtils;
import com.lihb.babyvoice.utils.RxBus;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class OperationActivity extends BaseFragmentActivity implements Observer {

    public static final String KEY_DATA = "key_data";
    private String TAG = "OperationActivity";


    private BleDevice bleDevice;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic characteristic;
    private int charaProp;

    private String[] titles = new String[3];
    private TitleBar mTitleBar;
    private ArrayList<byte[]> write_data = new ArrayList<byte[]>();//写入文件数据
    private String savePcmPath;
    private String saveWavPath;
    private boolean startWrite;

    private boolean isWriting;

    private AudioTrack audioTrack;

    private static final int FREQUENCY = 5000;// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static final int CHANNELCONGIFIGURATION = AudioFormat.CHANNEL_IN_MONO;// 设置单声道声道
    private static final int AUDIOENCODING = AudioFormat.ENCODING_PCM_8BIT;// 音频数据格式：每个样本16位
    private int recBufSize;// 录音最小buffer大小


    public void setWriting(boolean writing) {
        isWriting = writing;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_operation);
        FileUtils.createDirectory(Constant.DATA_DIRECTORY);
        initData();
        initPage();
        initView();

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
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(titles[2]);
        mTitleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        RxBus.getDefault().registerOnActivity(BluetoothCommand.class, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BluetoothCommand>() {
                    @Override
                    public void call(BluetoothCommand command) {
                        if (command.getStatus() == BluetoothCommand.BlueToothStatus.PHONE_SEND_HAND_SIGNAL) {
                            Logger.i("主机发起握手信号");
                        } else if (command.getStatus() == BluetoothCommand.BlueToothStatus.DEV_REPLY_HAND_SIGNAL) {
                            Logger.i("从机回复握手信号");
                        } else if (command.getStatus() == BluetoothCommand.BlueToothStatus.HEART_BEAT_SIGNAL) {
                            Log.i("心跳包: ", HexUtil.formatHexString(command.getData(), true));
//                            CommonToast.showShortToast("收到心跳包: " + HexUtil.formatHexString(command.getData(), true));
                        } else if (command.getStatus() == BluetoothCommand.BlueToothStatus.PHONE_STOP_SIGNAL) {
                            Logger.i("主机正常断开蓝牙前的通知数据包");
                        } else if (command.getStatus() == BluetoothCommand.BlueToothStatus.DEV_UPLOAD_VOICE_DATA_SIGNAL) {
                            final byte[] data = command.getData();
                            Log.d("lihb command", "从机上传实时胎心音数据: " + HexUtil.formatHexString(data, true));
                            int readSize = data.length;
                            audioTrack.write(data, 0, readSize);
                            if (startWrite) {
                                synchronized (write_data) {
                                    write_data.add(data);  // 实时播放数据
                                }
                            }
                        } else if (command.getStatus() == BluetoothCommand.BlueToothStatus.PHONE_SETTING_SIGNAL) {
                            Logger.i("主机配置设备的信息");
                        } else if (command.getStatus() == BluetoothCommand.BlueToothStatus.DEV_UPLOAD_STATUS_SIGNAL) {
                            Logger.i("从机主动上传设备状态信息");
                        } else if (command.getStatus() == BluetoothCommand.BlueToothStatus.DEV_UPLOAD_BATTERY_LEFT_SIGNAL) {
                            Logger.i("从机回复电池剩余可用监护时间");
                        } else if (command.getStatus() == BluetoothCommand.BlueToothStatus.DEV_PACKET_ERROR_SIGNAL) {
                            Logger.i("设备解析数据包时发现错误，进行信息返回。");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("[lihb command]", "headset out ,error: " + throwable.getMessage());
                    }
                });

    }

    public byte[] getBytes(short s) {
        byte[] buf = new byte[2];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) (s & 0x00ff);
            s >>= 8;
        }
        return buf;
    }

    private void initData() {
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null)
            finish();

        titles = new String[]{
                getString(R.string.service_list),
                getString(R.string.characteristic_list),
                getString(R.string.console)};
        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().equals(Constant.BlUETOOTH_SERVICE_UUID)) {
                bluetoothGattService = service;
                break;
            }
        }
    }

    private void initPage() {
        CharacteristicOperationFragment characteristicOperationFragment = new CharacteristicOperationFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment, characteristicOperationFragment).commit();

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

    public void startWriteFile() {
        isWriting = true;
        recBufSize = AudioRecord.getMinBufferSize(FREQUENCY,
                CHANNELCONGIFIGURATION, AUDIOENCODING);// 录音组件

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, FREQUENCY,
                AudioFormat.CHANNEL_OUT_MONO, AUDIOENCODING, recBufSize,
                AudioTrack.MODE_STREAM);

        new Thread(new WriteRunnable()).start();//开线程写文件
        audioTrack.play();

    }


    /**
     * 异步写文件
     *
     * @author cokus
     */
    class WriteRunnable implements Runnable {
        @Override
        public void run() {
            try {
                startWrite = true;
                savePcmPath = Constant.DATA_DIRECTORY + System.currentTimeMillis() + "test.pcm";
                saveWavPath = Constant.DATA_DIRECTORY + System.currentTimeMillis() + "test.wav";
                FileOutputStream fos2wav = null;
                File file2wav = null;
                try {
                    file2wav = new File(savePcmPath);
                    if (file2wav.exists()) {
                        file2wav.delete();
                    }
                    fos2wav = new FileOutputStream(file2wav);// 建立一个可存取字节的文件
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (isWriting || write_data.size() > 0) {
                    byte[] buffer = null;
                    synchronized (write_data) {
                        if (write_data.size() > 0) {
                            buffer = write_data.get(0);
                            write_data.remove(0);
                        }
                    }
                    try {
                        if (buffer != null) {
                            fos2wav.write(buffer);
                            fos2wav.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                fos2wav.close();
                Pcm2Wav p2w = new Pcm2Wav();//将pcm格式转换成wav 其实就尼玛加了一个44字节的头信息
                p2w.convertAudioFiles(savePcmPath, saveWavPath, (short) 8, 5000);
            } catch (Throwable t) {
                Log.e(TAG, "save file failed..");
            } finally {
                Log.i(TAG, "finally, always delete pcm file..");
                deleteFile(savePcmPath);
            }
        }
    }

    /**
     * 删除SD卡或者手机的缓存图片和目录
     */
    public boolean deleteFile(String filePath) {
        File dirFile = new File(filePath);
        if (!dirFile.exists()) {
            return false;
        }
        if (dirFile.isDirectory()) {
            String[] children = dirFile.list();
            for (int i = 0; i < children.length; i++) {
                File subFile = new File(dirFile.getAbsolutePath() + File.separator + children[i]);
                if (subFile.isDirectory()) {
                    deleteFile(subFile.getAbsolutePath());
                } else {
                    final File to = new File(subFile.getAbsolutePath() + System.currentTimeMillis());
                    subFile.renameTo(to);
                    to.delete();
//					new File(dirFile, children[i]).delete();
                }
            }
        }

        return dirFile.delete();
    }


}
