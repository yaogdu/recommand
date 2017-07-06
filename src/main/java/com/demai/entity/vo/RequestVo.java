package com.demai.entity.vo;

import java.io.Serializable;

/**
 * Created by dear on 16/3/9.
 */
public class RequestVo implements Serializable {


    private String data;

    private int data_type;

    private String verifystr;

    private long ruid;

    private long uid;

    public ClientInfo getClient_info() {
        return client_info;
    }

    public void setClient_info(ClientInfo client_info) {
        this.client_info = client_info;
    }

    private String apptoken;

    private ClientInfo client_info;

    public String getApptoken() {
        return apptoken;
    }

    public void setApptoken(String apptoken) {
        this.apptoken = apptoken;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getData_type() {
        return data_type;
    }

    public long getRuid() {
        return ruid;
    }

    public void setRuid(long ruid) {
        this.ruid = ruid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setData_type(int data_type) {
        this.data_type = data_type;
    }


    public String getVerifystr() {
        return verifystr;
    }

    public void setVerifystr(String verifystr) {
        this.verifystr = verifystr;
    }


}
