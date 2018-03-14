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

    @Override
    public String toString() {
        return "BabyBirthDay{" +
                "username='" + username + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}
