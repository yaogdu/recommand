package com.demai.entity.vo;

import java.io.Serializable;

/**
 * Created by dear on 15/9/12.
 */
public class ResultObject implements Serializable{
    public ResultObject(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResultObject(){

    }
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;
}
