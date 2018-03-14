package com.lihb.babyvoice.model;

import com.lihb.babyvoice.model.base.BaseRemindInfo;

/**
 * Created by lhb on 2017/3/6.
 */

public class PregnantRemindInfo extends BaseRemindInfo {

    /**
     * 产检名称
     */
    public String eventName;

    /**
     * 产检名称
     */
    public String eventNameEn;

    /**
     * 产检时间
     */
    public int eventDate;

    /**
     * 是否已读
     */
    public int hasRead;

    @Override
    public String toString() {
        return "PregnantRemindInfo{" +
                "eventName='" + eventName + '\'' +
                ", eventNameEn='" + eventNameEn + '\'' +
                ", eventDate=" + eventDate +
                ", hasRead=" + hasRead +
                '}';
    }
}
