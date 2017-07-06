package com.demai.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by dear on 16/2/24.
 */
public class RecommendLog implements Serializable {

    private long id;

    private int type;

    private long uid;

    private int pushed;//0 false,1 yes

    private int viewed;// 0 false,1 yes

    private int expired;

    public int getExpired() {
        return expired;
    }

    public void setExpired(int expired) {
        this.expired = expired;
    }

    public int getForbidden() {
        return forbidden;
    }

    public void setForbidden(int forbidden) {
        this.forbidden = forbidden;
    }

    private int forbidden;

    public int getPushed() {
        return pushed;
    }

    public void setPushed(int pushed) {
        this.pushed = pushed;
    }

    public int getViewed() {
        return viewed;
    }

    public void setViewed(int viewed) {
        this.viewed = viewed;
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public Date getViewTime() {
        return viewTime;
    }

    public void setViewTime(Date viewTime) {
        this.viewTime = viewTime;
    }

    private Date pushTime;//推送时间

    private Date viewTime;//浏览时间

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    private Date createTime;

    private long objectId;

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    private int source;//来源 0 计算 1 人工
}
