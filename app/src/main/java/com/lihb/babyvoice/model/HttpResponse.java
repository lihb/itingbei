package com.lihb.babyvoice.model;

/**
 * Created by lhb on 2017/2/20.
 */

public class HttpResponse<T> {
    public int code;
    public String msg;
    public T user;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return user;
    }

    public void setData(T data) {
        this.user = data;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + user +
                '}';
    }
}
