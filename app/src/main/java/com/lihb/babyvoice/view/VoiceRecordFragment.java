package com.lihb.babyvoice.view;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import com.lihb.babyvoice.Constant;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.customview.AnimatedRecordingView;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.utils.FileUtils;
import com.lihb.babyvoice.utils.PermissionCheckUtil;
import com.lihb.babyvoice.utils.RecorderHelper;
import com.lihb.babyvoice.utils.StringUtils;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;


/**
 * Created by lhb on 2017/2/10.
 */
public class VoiceRecordFragment extends BaseFragment {

    private static final String TAG = "VoiceRecordFragment";
    private TextView recordText;
    private Chronometer mChronometer;
    private TitleBar mTitleBar;
    private String mFileName;
    private int mRecordType;
    private AnimatedRecordingView mAnimatedRecordingView;
//    private RecordingView mRecordingView;

    public static VoiceRecordFragment create() {
        return new VoiceRecordFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            mRecordType = bundle.getInt("recordType");
        }
        return inflater.inflate(R.layout.fragment_voice_record, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FileUtils.createDirectory(Constant.DATA_DIRECTORY);
        initRecordHelper();
        initView();
//        ((NewMainActivity) getActivity()).toggleDrawableLayout(false);
    }

    private void initRecordHelper() {
        String[] items = getResources().getStringArray(R.array.voice_type);

        mFileName = items[mRecordType] + System.currentTimeMillis();

        RecorderHelper.getInstance().setPath(Constant.DATA_DIRECTORY + mFileName);
        RecorderHelper.getInstance().setRecorderListener(mOnRecorderListener);
    }


    private void initView() {
        mChronometer = (Chronometer) getView().findViewById(R.id.chronometer);
        recordText = (TextView) getView().findViewById(R.id.record_txt);
        recordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = recordText.getText().toString().trim();
                if (StringUtils.areEqual(text, "录制")) {
                    // 检测权限
                    if (PermissionCheckUtil.checkHasPermission(getActivity(), Manifest.permission.RECORD_AUDIO)) {
                        recordText.setText("完成");
                        RecorderHelper.getInstance().startRecord();
                    } else {
                        PermissionCheckUtil.showGrantFailDialog(getActivity(), getString(R.string.grant_audio_record_permission));
                    }
                } else {
//                    RecorderHelper.getInstance().cancel();
                    mAnimatedRecordingView.stop();
                    recordText.setText("录制");
                    gotoVoiceSaveFragment();

                }
            }
        });
        mTitleBar = (TitleBar) getView().findViewById(R.id.title_bar);
        mTitleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mAnimatedRecordingView = (AnimatedRecordingView) getView().findViewById(R.id.animated_recording_view);
//        mRecordingView = (RecordingView) getView().findViewById(R.id.animated_recording_view);
    }

    private RecorderHelper.onRecorderListener mOnRecorderListener = new RecorderHelper.onRecorderListener() {
        @Override
        public void recorderStart() {
            startChronometer(System.currentTimeMillis());
            mAnimatedRecordingView.start();
//            mRecordingView.start();
        }

        @Override
        public void recorderStop() {
            stopChronometer();
            mAnimatedRecordingView.stop();
        }

        @Override
        public void volumeChange(float vol) {
//            Log.e("lihb", "vol = " + vol);
            mAnimatedRecordingView.setVolume(vol);
//            mRecordingView.setVolume(vol);
        }
    };

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

        }
    };

    private long getPublishTime() {
        if (mChronometer == null) {
            return 0;
        }
        return System.currentTimeMillis() - mChronometer.getBase();
    }

    private VoiceSaveFragment mVoiceSaveFragment;

    private void gotoVoiceSaveFragment() {
        if (null == mVoiceSaveFragment) {
            mVoiceSaveFragment = VoiceSaveFragment.create();
        }
        Bundle bundle = new Bundle();
        bundle.putInt("type", mRecordType);
        bundle.putString("fileName", mFileName.substring(0, mFileName.indexOf(".")));
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
        MobclickAgent.onPageStart(TAG); //统计页面，"MainScreen"为页面名称，可自定义
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }


}