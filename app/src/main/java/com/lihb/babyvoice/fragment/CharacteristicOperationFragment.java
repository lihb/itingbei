package com.lihb.babyvoice.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.lihb.babyvoice.Constant;
import com.lihb.babyvoice.DataManager;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.activity.OperationActivity;
import com.lihb.babyvoice.utils.SoftInputUtil;
import com.lihb.babyvoice.utils.bluetooth.BluetoothParser;
import com.lihb.babyvoice.utils.bluetooth.CalcHeartRatioUtil;
import com.trello.rxlifecycle.components.support.RxFragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CharacteristicOperationFragment extends Fragment {

    public static final int PROPERTY_READ = 1;
    public static final int PROPERTY_WRITE = 2;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 3;
    public static final int PROPERTY_NOTIFY = 4;
    public static final int PROPERTY_INDICATE = 5;


    private LinearLayout layout_container;

    Subscription subscription = null;
    private List<Byte> commandDataList = new ArrayList<>();
    private CalcHeartRatioUtil calcHeartRatioUtil;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_characteric_operation, null);
        initView(v);
        showData();
        return v;
    }

    private void initView(View v) {
        layout_container = (LinearLayout) v.findViewById(R.id.layout_container);
    }

    public void showData() {
        final BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation, null);

        Button readBtn = (Button) view.findViewById(R.id.read_btn);
        EditText writeEditText = (EditText) view.findViewById(R.id.et);
        Button writeBtn = (Button) view.findViewById(R.id.write_btn);
        CheckBox timerSendCheckBox = (CheckBox) view.findViewById(R.id.timer_send_check_box);

        TextView contentTxt = (TextView) view.findViewById(R.id.content_txt);

        contentTxt.setMovementMethod(ScrollingMovementMethod.getInstance());
        contentTxt.clearFocus();
        writeEditText.clearFocus();
        writeEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                SoftInputUtil.hideSoftInput(getActivity());
            }
        }, 50);

        Observable observable = rx.Observable.interval(800, TimeUnit.MILLISECONDS).observeOn(Schedulers.io());

        timerSendCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    subscription = observable.subscribe(aLong -> {
                        Log.d("write data", "开始定时发送");
                        writeDataToDevice(writeEditText, contentTxt, bleDevice);
                    });
                } else {
                    cancelTimerSend();
                }
            }
        });
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeDataToDevice(writeEditText, contentTxt, bleDevice);
            }
        });
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (readBtn.getText().toString().equals("开始读数据")) {
                    readBtn.setText("停止读数据");
                    BleManager.getInstance().notify(
                            bleDevice,
                            Constant.BlUETOOTH_SERVICE_UUID,
                            Constant.BlUETOOTH_NOTIFY_UUID,
                            new BleNotifyCallback() {

                                @Override
                                public void onNotifySuccess() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addText(contentTxt, "notify success");
                                        }
                                    });
                                    ((OperationActivity) getActivity()).startWriteFile();
                                    DataManager.getInstance().setTransferDataStarted(true);
                                }

                                @Override
                                public void onNotifyFailure(final BleException exception) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addText(contentTxt, exception.toString());
                                        }
                                    });
                                }

                                @Override
                                public void onCharacteristicChanged(byte[] data) {

                                    Log.d("lihb getdata1 ", HexUtil.formatHexString(data, true));

                                    for (int i = 0; i < data.length; i++) {
                                        commandDataList.add(data[i]);
                                        if (data[i] == (byte) 0x55) {
                                            if (commandDataList.get(0) == (byte) 0xAA) {
                                                BluetoothParser.getInstance().parserBytes(commandDataList.toArray(new Byte[commandDataList.size()]));
                                                if (calcHeartRatioUtil == null) {
                                                    calcHeartRatioUtil = new CalcHeartRatioUtil((RxFragmentActivity) getActivity());
                                                    calcHeartRatioUtil.initRxBus();
                                                }
                                            }
                                            commandDataList.clear();
                                        }
                                    }


                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addText(contentTxt, HexUtil.formatHexString(data, true));

                                        }
                                    });
                                }
                            });
                } else {
                    readBtn.setText("开始读数据");
                    boolean isSuccess = BleManager.getInstance().stopNotify(
                            bleDevice,
                            Constant.BlUETOOTH_SERVICE_UUID,
                            Constant.BlUETOOTH_NOTIFY_UUID);
                    if (isSuccess) {
                        DataManager.getInstance().setTransferDataStarted(false);
                        ((OperationActivity) getActivity()).setWriting(false);
                        if (calcHeartRatioUtil != null) {
                            calcHeartRatioUtil.release();
                        }
                    }
                }
            }
        });
        layout_container.addView(view);
    }


    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

    private void writeDataToDevice(EditText writeEditText, final TextView contentText, BleDevice bleDevice) {
        String hex = writeEditText.getText().toString();
        if (TextUtils.isEmpty(hex)) {
            return;
        }
        byte[] data = HexUtil.hexStringToBytes(hex);
        Log.d("write data", Arrays.toString(data));
        BleManager.getInstance().write(
                bleDevice,
                Constant.BlUETOOTH_SERVICE_UUID,
                Constant.BlUETOOTH_WRITE_UUID,
                data,
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(contentText, "write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(contentText, exception.toString());
                            }
                        });
                    }
                });
    }

    /**
     * 取消定时发送握手包
     */
    private void cancelTimerSend() {
        Log.d("write data", "取消定时发送");
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTimerSend();
        DataManager.getInstance().setTransferDataStarted(false);
    }
}
