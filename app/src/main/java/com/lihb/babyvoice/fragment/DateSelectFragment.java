package com.lihb.babyvoice.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.lihb.babyvoice.BabyVoiceApp;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.db.impl.BirthdayDataImpl;
import com.lihb.babyvoice.db.impl.PregnantDateDataImpl;
import com.lihb.babyvoice.model.BabyBirthDay;
import com.lihb.babyvoice.model.PregnantDay;
import com.lihb.babyvoice.utils.SharedPreferencesUtil;
import com.orhanobut.logger.Logger;

import java.util.Calendar;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.lihb.babyvoice.R.id.datePicker;

/**
 * Created by lihb on 2017/3/5.
 */

public class DateSelectFragment extends BaseFragment {

    private EditText mLabelTxt;
    private DatePicker mDataPicker;
    private TextView mDoneTxt;
    private TitleBar mTitleBar;
    private int type;
    private int mSelYear, mSelMonth, mSelDay;

    public static DateSelectFragment create() {
        return new DateSelectFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle b = getArguments();
        if (null != b) {
            type = b.getInt("itemType");
        }
        View view = inflater.inflate(R.layout.fragment_date_select, container, false);
        initView(view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initView(View view) {
        mLabelTxt = (EditText) view.findViewById(R.id.label_txt);
        mDoneTxt = (TextView) view.findViewById(R.id.date_sel_done_txt);
        mTitleBar = (TitleBar) view.findViewById(R.id.title_bar);
        mTitleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mDataPicker = (DatePicker) view.findViewById(datePicker);

        Calendar calendar = Calendar.getInstance();
        mSelYear = calendar.get(Calendar.YEAR);
        mSelMonth = calendar.get(Calendar.MONTH) + 1;
        mSelDay = calendar.get(Calendar.DAY_OF_MONTH);
        mDataPicker.init(mSelYear, mSelMonth - 1, mSelDay, new DatePicker.OnDateChangedListener() {

            public void onDateChanged(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                mSelYear = year;
                mSelMonth = monthOfYear + 1;
                mSelDay = dayOfMonth;
                // 保存到SharedPreferences文件中
//                saveToSharedPreferences();
            }
        });


        mDoneTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type) {
                    case AssistFragment.ITEM_EXAMINE:
                        gotoNextFragment(PregnantExamineFragment.create());
                        break;
                    case AssistFragment.ITEM_VACCINE:
                        gotoNextFragment(VaccineFragment.create());
                        break;
                    case AssistFragment.ITEM_GROWUP:
                        gotoNextFragment(GrowUpFragment.create());
                        break;
                    case AssistFragment.ITEM_HEALTH_PROTECT:
                        gotoNextFragment(HealthShowFragment.create());
                        break;
                    case SettingFragment.ITEM_SET_BABY_BIRTHDAY:
                        getActivity().onBackPressed();
                        break;
                    case SettingFragment.ITEM_SET_PREGNANT_DATE:
                        getActivity().onBackPressed();
                        break;

                }
                // 保存到SharedPreferences文件中
                saveToSharedPreferences();
                saveToDatabase();
            }
        });
        setLabel();
    }

    private void saveToDatabase() {
        if (type == AssistFragment.ITEM_EXAMINE || type == SettingFragment.ITEM_SET_PREGNANT_DATE) {
            PregnantDay pregnantDay = new PregnantDay();
            pregnantDay.username = BabyVoiceApp.mUserInfo != null ? BabyVoiceApp.mUserInfo.username : "";
            pregnantDay.pregnantDay = mSelYear + "/" + mSelMonth + "/" + mSelDay;
            PregnantDateDataImpl.getInstance()
                    .insertData(pregnantDay)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            if (aBoolean) {
                                Logger.i("插入怀孕日期数据成功");
                            } else {
                                Logger.i("插入怀孕日期数据失败");
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.e(throwable.getMessage());
                        }
                    });
        } else {
            BabyBirthDay babyBirthDay = new BabyBirthDay();
            babyBirthDay.username = BabyVoiceApp.mUserInfo != null ? BabyVoiceApp.mUserInfo.username : "";
            babyBirthDay.birthday = mSelYear + "/" + mSelMonth + "/" + mSelDay;
            BirthdayDataImpl.getInstance()
                    .insertData(babyBirthDay)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            if (aBoolean) {
                                Logger.i("插入小孩出生日期数据成功");
                            } else {
                                Logger.i("插入小孩出生日期数据失败");
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.e(throwable.getMessage());
                        }
                    });
        }
    }

    private void saveToSharedPreferences() {
        if (type == AssistFragment.ITEM_EXAMINE || type == SettingFragment.ITEM_SET_PREGNANT_DATE) {
            SharedPreferencesUtil.setFirstPregnantDate(BabyVoiceApp.getInstance(), false);
            SharedPreferencesUtil.setPregnantDateInfo(BabyVoiceApp.getInstance(), mSelYear + "/" + mSelMonth + "/" + mSelDay);
        } else {
            SharedPreferencesUtil.setFirstBabyBirthday(BabyVoiceApp.getInstance(), false);
            SharedPreferencesUtil.setBabyBirthDayInfo(BabyVoiceApp.getInstance(), mSelYear + "/" + mSelMonth + "/" + mSelDay);
        }
    }

    private void setLabel() {
        switch (type) {
            case AssistFragment.ITEM_EXAMINE:
                mTitleBar.setLeftText(R.string.txt_check_assist);
                mLabelTxt.setText(R.string.enter_your_pregnant_date);
                break;
            case AssistFragment.ITEM_VACCINE:
                mTitleBar.setLeftText(R.string.txt_vaccine_assist);
                mLabelTxt.setText(R.string.enter_baby_birthday);
                break;
            case AssistFragment.ITEM_GROWUP:
                mTitleBar.setLeftText(R.string.txt_growup_record);
                mLabelTxt.setText(R.string.enter_baby_birthday);
                break;
            case AssistFragment.ITEM_HEALTH_PROTECT:
                mTitleBar.setLeftText(R.string.txt_health_protect_assist);
                mLabelTxt.setText(R.string.enter_baby_birthday);
                break;
            case SettingFragment.ITEM_SET_BABY_BIRTHDAY:
                mTitleBar.setLeftText("");
//                mLabelTxt.setText("请设置宝昵称");
                break;
            case SettingFragment.ITEM_SET_PREGNANT_DATE:
                mTitleBar.setLeftText("");
                mLabelTxt.setText(R.string.enter_your_pregnant_date);
                break;

        }
    }

    private void gotoNextFragment(BaseFragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this);
        String tag = "PregnantExamineFragment";
        if (fragment instanceof PregnantExamineFragment) {
            tag = "PregnantExamineFragment";
        } else if (fragment instanceof VaccineFragment) {
            tag = "VaccineFragment";
        } else if (fragment instanceof HealthShowFragment) {
            tag = "HealthShowFragment";
        } else if (fragment instanceof GrowUpFragment) {
            tag = "GrowUpFragment";
        }
        Bundle bundle = new Bundle();
        bundle.putInt("selYear", mSelYear);
        bundle.putInt("selMonth", mSelMonth);
        bundle.putInt("selDay", mSelDay);
        fragment.setArguments(bundle);
        transaction.add(R.id.main_layout, fragment, tag)
                .show(fragment)
                .addToBackStack(null)
                .commit();
    }

}
