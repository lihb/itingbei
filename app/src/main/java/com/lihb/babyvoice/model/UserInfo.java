package com.lihb.babyvoice.model;

/**
 * Created by lihb on 2018/4/7.
 */

public class UserInfo {


    /**
     * id : 17
     * lastdate : 1523040296313
     * mobile : 15820218025
     * money : 0
     * password : 31634
     * realname : 15820218025
     * regdate : 1523040296313
     * username : 15820218025
     * userstatus : true
     * uuid : E31330BB-EFFD-4133-9DB4-5243CDF69FE1
     */

    private int id;
    private long lastdate;
    private String mobile;
    private int money;
    private String password;
    private String realname;
    private long regdate;
    private String username;
    private boolean userstatus;
    private String uuid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getLastdate() {
        return lastdate;
    }

    public void setLastdate(long lastdate) {
        this.lastdate = lastdate;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public long getRegdate() {
        return regdate;
    }

    public void setRegdate(long regdate) {
        this.regdate = regdate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isUserstatus() {
        return userstatus;
    }

    public void setUserstatus(boolean userstatus) {
        this.userstatus = userstatus;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
