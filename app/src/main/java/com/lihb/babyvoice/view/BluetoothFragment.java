package com.lihb.babyvoice.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.utils.CommonToast;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by lhb on 2017/9/17.
 */

public class BluetoothFragment extends BaseFragment {

    private static final String TAG = "BluetoothFragment";
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.start_record)
    Button startRecord;
    @BindView(R.id.stop_record)
    Button stopRecord;
    @BindView(R.id.start_play)
    Button startPlay;
    @BindView(R.id.stop_play)
    Button stopPlay;
    Unbinder unbinder;

    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private AudioManager mAudioManager = null;


    public static BluetoothFragment create() {
        return new BluetoothFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        if (hidden == false) {
//            showBottomTab();
//        }
    }

    private void showBottomTab() {
        if (getActivity() == null) {
            return;
        }
        // 隐藏底部的导航栏和分割线
        ((LinearLayout) getActivity().findViewById(R.id.linearLayout1)).setVisibility(View.VISIBLE);
        ((View) getActivity().findViewById(R.id.divider_line2)).setVisibility(View.VISIBLE);
    }

    private void initView() {
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        titleBar.setLeftOnClickListener(v -> getActivity().onBackPressed());
    }


    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.start_record, R.id.stop_record, R.id.start_play, R.id.stop_play})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.start_record:
                startRecording();
                break;
            case R.id.stop_record:
                stopRecording();
                break;
            case R.id.start_play:
                startPlaying();
                break;
            case R.id.stop_play:
                stopPlaying();
                break;
        }
    }

    private void startRecording() {
        try {
            //获得文件保存路径。记得添加android.permission.WRITE_EXTERNAL_STORAGE权限
            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileName += "/btrecorder.wav";

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mRecorder.prepare();//如果文件打开失败，此步将会出错。
            if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
                Log.d(TAG, "系统不支持蓝牙录音");
                CommonToast.showShortToast("系统不支持蓝牙录音");
            }
        } catch (Exception e) {
            Log.e(TAG, "failed");
            CommonToast.showShortToast(e.getMessage());
        }
    }

    private void stopRecording() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            if (mAudioManager.isBluetoothScoOn()) {
                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.stopBluetoothSco();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            CommonToast.showShortToast(e.getMessage());

        }

    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            if (!mAudioManager.isBluetoothA2dpOn())
                mAudioManager.setBluetoothA2dpOn(true); //如果A2DP没建立，则建立A2DP连接
            mAudioManager.stopBluetoothSco();//如果SCO没有断开，由于SCO优先级高于A2DP，A2DP可能无声音
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
            //让声音路由到蓝牙A2DP。此方法虽已弃用，但就它比较直接、好用。
            mAudioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_BLUETOOTH_A2DP, AudioManager.ROUTE_BLUETOOTH);
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();


            //蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
            mAudioManager.startBluetoothSco();
            //蓝牙SCO连接建立需要时间，连接建立后会发出ACTION_SCO_AUDIO_STATE_CHANGED消息，通过接收该消息而进入后续逻辑。
            //也有可能此时SCO已经建立，则不会收到上述消息，可以startBluetoothSco()前先stopBluetoothSco()
            getActivity().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

                    if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                        mAudioManager.setBluetoothScoOn(true);  //打开SCO
                        mRecorder.start();//开始录音
                        getActivity().unregisterReceiver(this);  //别遗漏
                    } else {//等待一秒后再尝试启动SCO
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mAudioManager.startBluetoothSco();
                    }
                }
            }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
        } catch (Exception e) {
            Log.e(TAG, "prepare() failed");
            CommonToast.showShortToast(e.getMessage());
        }
    }

    private void stopPlaying() {
        try {
            mPlayer.release();
            mPlayer = null;
            mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            CommonToast.showShortToast(e.getMessage());
        }

    }
}
