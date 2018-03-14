package com.lihb.babyvoice.model;

import com.lihb.babyvoice.model.base.BaseRemindInfo;

/**
 * Created by lhb on 2017/3/6.
 */

public class VaccineRemindInfo extends BaseRemindInfo {

    /**
     * 疫苗名称
     */
    public String vaccineName;

    /**
     * 疫苗名称
     */
    public String vaccineNameEn;

    /**
     * 注射该疫苗时，小孩所需年龄
     */
    public int ageToInject;

    /**
     * 是否已读
     */
    public int hasRead;

    @Override
    public String toString() {
        return "VaccineRemindInfo{" +
                "vaccineName='" + vaccineName + '\'' +
                ", vaccineNameEn='" + vaccineNameEn + '\'' +
                ", ageToInject=" + ageToInject +
                ", hasRead=" + hasRead +
                '}';
    }
}
