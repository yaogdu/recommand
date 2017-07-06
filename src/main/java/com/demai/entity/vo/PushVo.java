package com.demai.entity.vo;

import java.io.Serializable;

/**
 * Created by dear on 16/3/22.
 */
public class PushVo implements Serializable {

    private long id;

    private String uids;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUids() {
        return uids;
    }

    public void setUids(String uids) {
        this.uids = uids;
    }

    public String getManUids() {
        return manUids;
    }

    public void setManUids(String manUids) {
        this.manUids = manUids;
    }

    private String manUids;//人工输入uid推荐
}
