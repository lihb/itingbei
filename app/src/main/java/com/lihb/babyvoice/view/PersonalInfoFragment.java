package com.lihb.babyvoice.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lihb.babyvoice.BabyVoiceApp;
import com.lihb.babyvoice.R;
import com.lihb.babyvoice.action.ApiManager;
import com.lihb.babyvoice.action.ResponseCode;
import com.lihb.babyvoice.action.ServiceGenerator;
import com.lihb.babyvoice.command.UpdateUserInfoItemCommand;
import com.lihb.babyvoice.customview.CommonItem;
import com.lihb.babyvoice.customview.TitleBar;
import com.lihb.babyvoice.customview.base.BaseFragment;
import com.lihb.babyvoice.model.HttpResponse;
import com.lihb.babyvoice.presenter.profile.PersonalInfoPresenter;
import com.lihb.babyvoice.utils.CommonDialog;
import com.lihb.babyvoice.utils.CommonToast;
import com.lihb.babyvoice.utils.RxBus;
import com.lihb.babyvoice.utils.SimpleDatePickerDialog;
import com.lihb.babyvoice.utils.StringUtils;
import com.lihb.babyvoice.utils.camera.PhotoHelper;
import com.lihb.babyvoice.view.profile.PersonalInfoMvpView;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lhb on 2017/2/8.
 */

public class PersonalInfoFragment extends BaseFragment implements PersonalInfoMvpView {

    private static final String TAG = "PersonalInfoFragment";

    private static final int AVATAR_WIDTH_HEIGHT = 480;

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
    @BindView(R.id.item_user_Avatar)
    CommonItem itemUserAvatar;

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
        receiveUpdateEvent();
        return view;
    }

//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//        if (!hidden) {
//            Bundle b = getArguments();
//            if (null != b) {
//                content = b.getString("content");
//                currChangeItem = b.getInt("itemIndex");
//            }
//            testUpdateContent();
//        }
//    }

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

        itemUserAvatar.setOnClickListener(v -> {
            selectImageFromAlbum();
        });

    }

    private void selectImageFromAlbum() {
        PhotoHelper.create(getActivity())
//                .setSourceGallery()
                .setPreferredSize(AVATAR_WIDTH_HEIGHT, AVATAR_WIDTH_HEIGHT)
                .start();
    }


    public void updatePhoto(String picturePath) {
        if (TextUtils.isEmpty(picturePath)) {
            return;
        }
        itemUserAvatar.setUserAvatar(picturePath);
        // 更新头像到服务器
        uploadPicToServer(picturePath);

    }

    /**
     * 上传图片到服务器
     */
    private void uploadPicToServer(String filePath) {
        List<File> fileList = new ArrayList<>();
        File file = null;
        if (!StringUtils.isBlank(filePath)) {
            file = new File(filePath);
        }

        if (file == null || !file.exists()) {
            Logger.i("no pic to upload!!");
            return;
        }
        fileList.add(file);
        MultipartBody body = filesToMultipartBody(fileList);
        ServiceGenerator.createService(ApiManager.class)
                .uploadPicFile(BabyVoiceApp.uuid, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<HttpResponse<Void>>() {
                    @Override
                    public void call(HttpResponse<Void> stringBaseResponse) {
                        Logger.i(stringBaseResponse.msg);
                        if (stringBaseResponse.code == ResponseCode.RESPONSE_OK) {
                            Logger.i("upload pic success.");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.e(throwable.getMessage());
                    }
                });
    }


    public static MultipartBody filesToMultipartBody(List<File> files) {
        MultipartBody.Builder builder = new MultipartBody.Builder();

        for (File file : files) {
            RequestBody requestBody = RequestBody.create(MediaType.parse(""), file);
            builder.addFormDataPart("picfile", file.getName(), requestBody);
//            builder.addFormDataPart("fileName", file.getName());
        }
        builder.setType(MultipartBody.FORM);
        MultipartBody multipartBody = builder.build();
        return multipartBody;

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
    public void onUpdatePhoneNum(String phoneNum) {
        itemMobilePhone.setItemValue(phoneNum);
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
                        String date = datePickerDialog.getYYYYMMDD("%04d-%02d-%02d");
                        if (currItem == ITEM_BIRTHDAY) {
                            itemBirthday.setItemValue(date);
                        } else if (currItem == ITEM_DUE_DATE) {
                            itemDueDate.setItemValue(date);
                        }

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

    private void receiveUpdateEvent() {

        RxBus.getDefault().registerOnFragment(UpdateUserInfoItemCommand.class, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UpdateUserInfoItemCommand>() {
                    @Override
                    public void call(UpdateUserInfoItemCommand command) {
                        currChangeItem = command.itemIndex;
                        content = command.content;
                        Logger.i("index is %d, content is %s", currChangeItem, content);
                        testUpdateContent();

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.e(throwable, "error");
                    }
                });
    }

}
