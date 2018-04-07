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

    public String username;
    public String password;
    public String realname;
    public String telephone;
    public String mobile;
    public String userSex;
    public String province;
    public String city;
    public String county;
    public String gdcode;
    public String email;
    public String cardID;
    public int department;
    public boolean userstatus;
    public String regDate;
    public String address;
    public String lastDate;
    public String ipaddres;
    public String uuid;
    public String nickname;
    public String birthday;
    public String duedate;
    public String qq;
    public String headicon;
    public float money;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUserSex() {
        return userSex;
    }

    public void setUserSex(String userSex) {
        this.userSex = userSex;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getGdcode() {
        return gdcode;
    }

    public void setGdcode(String gdcode) {
        this.gdcode = gdcode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public int getDepartment() {
        return department;
    }

    public void setDepartment(int department) {
        this.department = department;
    }

    public boolean getUserstatus() {
        return userstatus;
    }

    public void setUserstatus(boolean userstatus) {
        this.userstatus = userstatus;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public String getIpaddres() {
        return ipaddres;
    }

    public void setIpaddres(String ipaddres) {
        this.ipaddres = ipaddres;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String duedate) {
        this.duedate = duedate;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getHeadicon() {
        return headicon;
    }

    public void setHeadicon(String headicon) {
        this.headicon = headicon;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }


    @Override
    public String toString() {
        return "UserInfo{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", realname='" + realname + '\'' +
                ", telephone='" + telephone + '\'' +
                ", mobile='" + mobile + '\'' +
                ", userSex='" + userSex + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", county='" + county + '\'' +
                ", gdcode='" + gdcode + '\'' +
                ", email='" + email + '\'' +
                ", cardID='" + cardID + '\'' +
                ", department=" + department +
                ", userstatus=" + userstatus +
                ", regDate='" + regDate + '\'' +
                ", address='" + address + '\'' +
                ", lastDate='" + lastDate + '\'' +
                ", ipaddres='" + ipaddres + '\'' +
                ", uuid='" + uuid + '\'' +
                ", nickname='" + nickname + '\'' +
                ", birthday='" + birthday + '\'' +
                ", duedate='" + duedate + '\'' +
                ", qq='" + qq + '\'' +
                ", headicon='" + headicon + '\'' +
                ", money=" + money +
                '}';
    }
}
