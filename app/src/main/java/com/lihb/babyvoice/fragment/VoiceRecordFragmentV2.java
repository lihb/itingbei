package com.lihb.babyvoice.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.cokus.wavelibrary.draw.WaveCanvas;
import com.cokus.wavelibrary.view.WaveSurfaceView;
import com.lihb.babyvoice.Constant;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.command.HeadSetPluginChangedCommand;
import com.lihb.babyvoice.command.PickedCategoryCommand;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.utils.CommonToast;
import com.lihb.babyvoice.utils.FileUtils;
import com.lihb.babyvoice.utils.PermissionCheckUtil;
import com.lihb.babyvoice.utils.RxBus;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by lhb on 2017/2/10.
 */
public class VoiceRecordFragmentV2 extends BaseFragment {

    private static final String TAG = "VoiceRecordFragmentV2";
    private TextView recordText;
    private Chronometer mChronometer;
    private TitleBar mTitleBar;
    private String mFileName = "test";
    private int mRecordType;
    private WaveSurfaceView mWaveSfv;
    private WaveCanvas waveCanvas;
    private ImageView voicePosImg;

    private static final int FREQUENCY = 16000;// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static final int CHANNELCONGIFIGURATION = AudioFormat.CHANNEL_IN_MONO;// 设置单声道声道
    private static final int AUDIOENCODING = AudioFormat.ENCODING_PCM_16BIT;// 音频数据格式：每个样本16位
    public final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;// 音频获取源
    private int recBufSize;// 录音最小buffer大小
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private boolean mIsBegin = false;
    public static final int TYPE_HEART_TIME = 20 * 60 * 1000;
    public static final int TYPE_LUNG_TIME = 30 * 1000;


    public static VoiceRecordFragmentV2 create() {
        return new VoiceRecordFragmentV2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mRecordType = bundle.getInt("recordType");
        }
        return inflater.inflate(R.layout.fragment_voice_recordv2, container, false);
    }

    @Override
    public boolean onBackPressed() {
        if (null != waveCanvas) {
            waveCanvas.stop();
        }
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hideBottomTab();
        initView();
        FileUtils.createDirectory(Constant.DATA_DIRECTORY);
        if (mWaveSfv != null) {
            mWaveSfv.setLine_off(42);
            //解决surfaceView黑色闪动效果
            mWaveSfv.setZOrderOnTop(true);
            mWaveSfv.getHolder().setFormat(PixelFormat.TRANSPARENT);
        }

        RxBus.getDefault().registerOnFragment(HeadSetPluginChangedCommand.class, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<HeadSetPluginChangedCommand>() {
                    @Override
                    public void call(HeadSetPluginChangedCommand headSetPluginChangedCommand) {
                        if (headSetPluginChangedCommand.getState() == HeadSetPluginChangedCommand.HeadSetPluginState.HEAD_SET_OUT) {
                            Logger.i("headset is out!!");

                            if ((mRecordType == PickedCategoryCommand.TYPE_HEART || mRecordType == PickedCategoryCommand.TYPE_LUNG) && mIsBegin) {
                                CommonToast.showLongToast(R.string.plugin_headset_first);
                                if (null != waveCanvas) {
                                    waveCanvas.stop();
                                }
                                getActivity().onBackPressed();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.e("headset out ,error: %s", throwable.getMessage());
                    }
                });
//        ((NewMainActivity) getActivity()).toggleDrawableLayout(false);

        record();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden == false) {
            hideBottomTab();
            mChronometer.setBase(System.currentTimeMillis());
            if (mWaveSfv != null) {
                mWaveSfv.initDraw();
            }

        }
    }

    /**
     * 开始录制
     */
    private void record() {
        // 权限检测
        String permissions[] = new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (PermissionCheckUtil.checkPermissionAllGranted(getActivity(), permissions)) {
            initAudio();
        } else {
            requestPermissions(permissions, PermissionCheckUtil.REQUEST_PERMISSION);
//            PermissionCheckUtil.showGrantFailDialog(getActivity(), getString(R.string.grant_audio_record_permission));
        }
    }


    private void hideBottomTab() {
        if (getActivity() == null) {
            return;
        }
        // 隐藏底部的导航栏和分割线
        (getActivity().findViewById(R.id.tab_layout)).setVisibility(View.GONE);
        (getActivity().findViewById(R.id.main_divider_line)).setVisibility(View.GONE);
    }

    private void initView() {
        mChronometer = (Chronometer) getView().findViewById(R.id.chronometer);
        recordText = (TextView) getView().findViewById(R.id.record_txt);
        voicePosImg = (ImageView) getView().findViewById(R.id.voice_pos_img);

        recordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recordText.getText().toString().equals("录制")) {
                    recordText.setText("完成");
                    if (waveCanvas != null) {
                        if (!mIsBegin) {
                            record();
                        }
                        waveCanvas.startWriteFile();
                        voicePosImg.setVisibility(View.GONE);
                    } else {
                        CommonToast.showShortToast("发生错误，请重新打开该页面");
                    }
                } else if (recordText.getText().toString().equals("完成")) {
                    recordText.setText("录制");
                    waveCanvas.stop();
                    gotoVoiceSaveFragment();
                    voicePosImg.setVisibility(View.VISIBLE);
                }
            }
        });
        mTitleBar = (TitleBar) getView().findViewById(R.id.title_bar);
        mTitleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != waveCanvas) {
                    waveCanvas.stop();
                }
                getActivity().onBackPressed();
            }
        });

        mWaveSfv = (WaveSurfaceView) getView().findViewById(R.id.wavesfv);

        configAudioSetting();

    }

    private void configAudioSetting() {
        recBufSize = AudioRecord.getMinBufferSize(FREQUENCY,
                CHANNELCONGIFIGURATION, AUDIOENCODING);// 录音组件

        audioRecord = new AudioRecord(AUDIO_SOURCE,// 指定音频来源，这里为麦克风
                FREQUENCY, // 16000HZ采样频率
                CHANNELCONGIFIGURATION,// 录制通道
                AUDIO_SOURCE,// 录制编码格式
                recBufSize);// 录制缓冲区大小 //先修改

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, FREQUENCY,
                AudioFormat.CHANNEL_OUT_MONO, AUDIOENCODING, recBufSize,
                AudioTrack.MODE_STREAM);
    }


    public void initAudio() {

        String[] items = getResources().getStringArray(R.array.voice_type);

        if (waveCanvas == null) {
            waveCanvas = new WaveCanvas();
        }
        waveCanvas.baseLine = mWaveSfv.getHeight() / 2;

        mFileName = items[mRecordType] + System.currentTimeMillis();
        waveCanvas.startRecord(audioRecord, audioTrack, recBufSize, mWaveSfv, mFileName, Constant.DATA_DIRECTORY, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == WaveCanvas.MSG_SINGAL_START && !mIsBegin) {
                    mIsBegin = true;
                    startChronometer(System.currentTimeMillis());
                } else if (msg.what == WaveCanvas.MSG_SINGAL_STOP) {
                    mIsBegin = false;
                    stopChronometer();
                } else {
                    mIsBegin = false;
                    String errorMsg = (String) msg.obj;
                    Log.e("lihb data", "error: " + errorMsg);
                }
                return true;
            }
        });
    }

    public void startChronometer(long startTime) {
        mChronometer.setBase(startTime);
        mChronometer.setOnChronometerTickListener(mOnChronometerTickListener);
        mChronometer.start();
    }

    public long stopChronometer() {
        if (mChronometer != null) {
            mChronometer.stop();
            long t = System.currentTimeMillis() - mChronometer.getBase();
            return t;
        }
        return 0;
    }

    public long getChronometerRunningTime() {
        if (mChronometer == null) {
            return 0;
        }
        return System.currentTimeMillis() - mChronometer.getBase();
    }

    /**
     * 计时器的时间变化监听
     */
    //region 计时器监听器
    private Chronometer.OnChronometerTickListener mOnChronometerTickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer var1) {
            if (mChronometer == null)
                return;

            try {
                long interval = getPublishTime() / 1000;
                int minutes = (int) (interval / 60);
                int seconds = (int) (interval % 60);
                int hours = minutes / 60;
                if (hours > 0) {
                    minutes = minutes % 60;
                }
                int days = hours / 24;
                if (days > 0) {
                    hours = hours % 24;
                }

                StringBuffer stringBuffer = new StringBuffer();
                if (days > 0) {
                    stringBuffer.append(String.format("%02d", days) + "天");
                }
                stringBuffer.append(String.format("%02d", hours) + ":");
                stringBuffer.append(String.format("%02d", minutes) + ":");
                stringBuffer.append(String.format("%02d", seconds));
                mChronometer.setText(stringBuffer.toString());
            } catch (Exception e) {
                Logger.i("Update video time failed:%s", e.toString());
            }
            // 胎心音录制时间不超过20分钟，肺音不超过30s
            if ((mRecordType == PickedCategoryCommand.TYPE_HEART && getPublishTime() >= TYPE_HEART_TIME)
                    || (mRecordType == PickedCategoryCommand.TYPE_LUNG && getPublishTime() >= TYPE_LUNG_TIME)) {
                waveCanvas.stop();
            }

        }
    };

    //endregion

    private long getPublishTime() {
        if (mChronometer == null) {
            return 0;
        }
        return System.currentTimeMillis() - mChronometer.getBase();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (waveCanvas != null) {
            waveCanvas.stop();
            waveCanvas = null;
        }
    }

    private VoiceSaveFragment mVoiceSaveFragment;

    private void gotoVoiceSaveFragment() {
        if (null == mVoiceSaveFragment) {
            mVoiceSaveFragment = VoiceSaveFragment.create();
        }
        Bundle bundle = new Bundle();
        bundle.putInt("type", mRecordType);
        bundle.putString("fileName", mFileName);
        Log.e("VoiceRecordFragmentV2", "gotoVoiceSaveFragment: filename = " + mFileName);
        mVoiceSaveFragment.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this);
        transaction.add(R.id.main_layout, mVoiceSaveFragment, "VoiceSaveFragment")
                .show(mVoiceSaveFragment)
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionCheckUtil.REQUEST_PERMISSION) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                initAudio();

            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                PermissionCheckUtil.showGrantFailDialog(getActivity(), getString(R.string.grant_audio_record_permission));
            }
        }
    }


}