package com.lihb.babyvoice.model;

/**
 * Created by lhb on 2017/3/6.
 */

public class BabyBirthDay {

    /**
     * 用户名
     */
    public String username;

    /**
     * 出生日期
     */
    public String birthday;


    /**
     * 是否当前选择
     *
     * @return
     */

    public boolean isSelected;

    /**
     * 宝贝头像
     */
    public String babyAvatar;

    @Override
    public String toString() {
        return "BabyBirthDay{" +
                "username='" + username + '\'' +
                ", birthday='" + birthday + '\'' +
                ", isSelected=" + isSelected +
                ", babyAvatar='" + babyAvatar + '\'' +
                '}';
    }
}
