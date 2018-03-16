package com.lihb.babyvoice.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.customview.CommonItem;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.presenter.profile.PersonalInfoPresenter;
import com.lihb.babyvoice.utils.CommonDialog;
import com.lihb.babyvoice.utils.CommonToast;
import com.lihb.babyvoice.utils.SimpleDatePickerDialog;
import com.lihb.babyvoice.view.profile.PersonalInfoMvpView;
import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by lhb on 2017/2/8.
 */

public class PersonalInfoFragment extends BaseFragment implements PersonalInfoMvpView {

    private static final String TAG = "PersonalInfoFragment";

    public static final int ITEM_NICK_NAME = 1;
    public static final int ITEM_PHONE = 2;
    public static final int ITEM_EMAIL = 3;
    public static final int ITEM_QQ_NUMBER = 4;
    public static final int ITEM_NAME = 5;
    public static final int ITEM_ADDRESS = 6;
    public static final int ITEM_BIRTHDAY = 7;
    public static final int ITEM_DUE_DATE = 8;
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.item_nick_name)
    CommonItem itemNickName;
    @BindView(R.id.item_mobile_phone)
    CommonItem itemMobilePhone;
    @BindView(R.id.item_email)
    CommonItem itemEmail;
    @BindView(R.id.item_birthday)
    CommonItem itemBirthday;
    @BindView(R.id.item_due_date)
    CommonItem itemDueDate;
    @BindView(R.id.item_qq_number)
    CommonItem itemQQNumber;
    @BindView(R.id.item_user_name)
    CommonItem itemName;
    @BindView(R.id.item_address)
    CommonItem itemAddress;
    @BindView(R.id.additional_item_container)
    LinearLayout additionalItemContainer;
    Unbinder unbinder;
    @BindView(R.id.item_baby_info)
    CommonItem itemBabyInfo;

    private PersonalInfoChangeFragment mPersonalInfoChangeFragment;

    private DateSelectFragment mDateSelectFragment;

    private int currItem = ITEM_NICK_NAME;

    private SimpleDatePickerDialog datePickerDialog;

    private String content;
    private int currChangeItem;
    private BabyInfoFragment mBabyInfoFragment;

    public static PersonalInfoFragment create() {
        return new PersonalInfoFragment();
    }

    private PersonalInfoPresenter personalInfoPresenter;
    final String now = new SimpleDateFormat("yyyyMMdd").format(new Date());


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle b = getArguments();
        if (null != b) {
            content = b.getString("content");
            currChangeItem = b.getInt("itemIndex");
        }
        View view = inflater.inflate(R.layout.fragment_personal_setting, container, false);
        unbinder = ButterKnife.bind(this, view);
        testUpdateContent();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Bundle b = getArguments();
            if (null != b) {
                content = b.getString("content");
                currChangeItem = b.getInt("itemIndex");
            }
            testUpdateContent();
        }
    }

    private void testUpdateContent() {
        switch (currChangeItem) {
            case ITEM_NICK_NAME:
                onUpdateNickName(content);
                break;
            case ITEM_PHONE:
                onUpdatePhoneNum(content);
                break;
            case ITEM_EMAIL:
                onUpdateEmail(content);
                break;
            case ITEM_QQ_NUMBER:
                onUpdateQQ(content);
                break;
            case ITEM_NAME:
                onUpdateName(content);
                break;
            case ITEM_ADDRESS:
                onUpdateAddress(content);
                break;
            default:
                break;

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {

        titleBar.setLeftOnClickListener(v -> getActivity().onBackPressed());

        itemNickName.setOnClickListener(v -> {
            currItem = ITEM_NICK_NAME;
            gotoPersonalInfoChangeFragment(itemNickName.getItemValue());
        });
        itemMobilePhone.setOnClickListener(v -> {
            currItem = ITEM_PHONE;
            gotoPersonalInfoChangeFragment(itemMobilePhone.getItemValue());
        });
        itemEmail.setOnClickListener(v -> {
            currItem = ITEM_EMAIL;
            gotoPersonalInfoChangeFragment(itemEmail.getItemValue());
        });
        itemQQNumber.setOnClickListener(v -> {
            currItem = ITEM_QQ_NUMBER;
            gotoPersonalInfoChangeFragment(itemQQNumber.getItemValue());
        });
        itemName.setOnClickListener(v -> {
            currItem = ITEM_NAME;
            gotoPersonalInfoChangeFragment(itemName.getItemValue());
        });
        itemAddress.setOnClickListener(v -> {
            currItem = ITEM_ADDRESS;
            gotoPersonalInfoChangeFragment(itemAddress.getItemValue());
        });
        itemBirthday.setOnClickListener(v -> {
            currItem = ITEM_BIRTHDAY;
            showSelectDateDialog(ITEM_BIRTHDAY);
        });
        itemDueDate.setOnClickListener(v -> {
            currItem = ITEM_DUE_DATE;
            showSelectDateDialog(ITEM_DUE_DATE);
        });

        itemBabyInfo.setOnClickListener(v -> {
//            gotoDateSelectFragment(MeFragment.ITEM_SET_BABY_BIRTHDAY);
            gotoBabyInfoFragment();
        });

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
    protected void attachPresenters() {
        super.attachPresenters();
        personalInfoPresenter = new PersonalInfoPresenter();
        personalInfoPresenter.attachView(this);
    }

    @Override
    protected void detachPresenters() {
        super.detachPresenters();
        personalInfoPresenter.detachView();
    }

    @Override
    public void onUpdateNickName(String nickName) {
        itemNickName.setItemValue(nickName);
    }

    @Override
    public void onUpdateName(String name) {
        itemName.setItemValue(name);
    }

    @Override
    public void onUpdateEmail(String email) {
        itemEmail.setItemValue(email);
    }

    @Override
    public void onUpdateAddress(String address) {
        itemAddress.setItemValue(address);
    }

    @Override
    public void onUpdateQQ(String qq) {
        itemQQNumber.setItemValue(qq);
    }

    @Override
    public void onUpdateBirthday(String birthday) {
        itemBirthday.setItemValue(birthday);
    }

    @Override
    public void onUpdatePhoneNum(String phoneNum) {
        itemMobilePhone.setItemValue(phoneNum);
    }

    @Override
    public void onUpdateDueDate(String dueDate) {
        itemDueDate.setItemValue(dueDate);
    }

    @Override
    public void onError(String message) {
        CommonToast.showShortToast(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void showSelectDateDialog(final int currItem) {
        datePickerDialog = new SimpleDatePickerDialog.Builder()
                .setContext(getActivity())
                .setTitleId(R.string.select_date)
                .setYYYYMMDD(now)
                .setConfirmListener(new CommonDialog.OnActionListener() {
                    @Override
                    public void onAction(int which) {
                        String date = datePickerDialog.getYYYYMMDD();
                        if (currItem == ITEM_BIRTHDAY) {
                            itemBirthday.setItemValue(date);
                        } else if (currItem == ITEM_DUE_DATE) {
                            itemDueDate.setItemValue(date);
                        }
                        // FIXME: 2017/9/17 同步服务器接口

                    }
                })
                .build();
        datePickerDialog.show();
    }

    private void gotoPersonalInfoChangeFragment(String content) {
        if (null == mPersonalInfoChangeFragment) {
            mPersonalInfoChangeFragment = PersonalInfoChangeFragment.create();
        }
        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        bundle.putInt("itemIndex", currItem);
        mPersonalInfoChangeFragment.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this);
        transaction.add(R.id.main_layout, mPersonalInfoChangeFragment, "PersonalInfoChangeFragment")
                .show(mPersonalInfoChangeFragment)
                .addToBackStack(null)
                .commit();
    }


    private void gotoBabyInfoFragment() {
        if (null == mBabyInfoFragment) {
            mBabyInfoFragment = BabyInfoFragment.create();
        }

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this);
        transaction.add(R.id.main_layout, mBabyInfoFragment, "BabyInfoFragment")
                .show(mBabyInfoFragment)
                .addToBackStack(null)
                .commit();

    }

}
