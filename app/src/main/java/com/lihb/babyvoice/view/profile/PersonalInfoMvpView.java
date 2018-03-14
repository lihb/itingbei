package com.lihb.babyvoice.view.profile;

import com.lihb.babyvoice.view.IView;

/**
 * Created by Administrator on 2017/9/16.
 */

public interface PersonalInfoMvpView extends IView {
    void onUpdateNickName(String nickName);

    void onUpdateName(String name);

    void onUpdateEmail(String email);

    void onUpdateAddress(String address);

    void onUpdateQQ(String qq);

    void onUpdateBirthday(String birthday);

    void onUpdatePhoneNum(String phoneNum);

    void onUpdateDueDate(String dueDate);
}
