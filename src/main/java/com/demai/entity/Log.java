package com.demai.entity;

import java.io.Serializable;

public class Log implements Serializable {
    private String uid;
    private String logType;
    private String logTime;
    private String appType;
    private String deviceID;
    private String osType;
    private String osVersion;
    private String network;
    private String appVersion;
    private String method;
    private String keyword;
    private String registerSource;
    private String beViewedUID;
    private String operation;
    private String modifyContent;
    private String operationSource;
    private String poi;
    private String bePraisedUID;
    private String praiseType;
    private String beCommentedUID;
    private String commentType;
    private String receivedUID;
    private String messageType;
    private String isGroupSend;
    private String beFocusonUID;
    private String focusonType;
    private String publishType;
    private String isPublic;
    private String beBrowsedUID;

    private String beBrowsedContentID;
    private String beBrowsedContentType;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getRegisterSource() {
        return registerSource;
    }

    public void setRegisterSource(String registerSource) {
        this.registerSource = registerSource;
    }

    public String getBeViewedUID() {
        return beViewedUID;
    }

    public void setBeViewedUID(String beViewedUID) {
        this.beViewedUID = beViewedUID;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getModifyContent() {
        return modifyContent;
    }

    public void setModifyContent(String modifyContent) {
        this.modifyContent = modifyContent;
    }

    public String getOperationSource() {
        return operationSource;
    }

    public void setOperationSource(String operationSource) {
        this.operationSource = operationSource;
    }

    public String getPoi() {
        return poi;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }

    public String getBePraisedUID() {
        return bePraisedUID;
    }

    public void setBePraisedUID(String bePraisedUID) {
        this.bePraisedUID = bePraisedUID;
    }

    public String getPraiseType() {
        return praiseType;
    }

    public void setPraiseType(String praiseType) {
        this.praiseType = praiseType;
    }

    public String getBeCommentedUID() {
        return beCommentedUID;
    }

    public void setBeCommentedUID(String beCommentedUID) {
        this.beCommentedUID = beCommentedUID;
    }

    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public String getReceivedUID() {
        return receivedUID;
    }

    public void setReceivedUID(String receivedUID) {
        this.receivedUID = receivedUID;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getIsGroupSend() {
        return isGroupSend;
    }

    public void setIsGroupSend(String isGroupSend) {
        this.isGroupSend = isGroupSend;
    }

    public String getBeFocusonUID() {
        return beFocusonUID;
    }

    public void setBeFocusonUID(String beFocusonUID) {
        this.beFocusonUID = beFocusonUID;
    }

    public String getFocusonType() {
        return focusonType;
    }

    public void setFocusonType(String focusonType) {
        this.focusonType = focusonType;
    }

    public String getPublishType() {
        return publishType;
    }

    public void setPublishType(String publishType) {
        this.publishType = publishType;
    }

    public String getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
    }

    public String getBeBrowsedUID() {
        return beBrowsedUID;
    }

    public void setBeBrowsedUID(String beBrowsedUID) {
        this.beBrowsedUID = beBrowsedUID;
    }


    public String getBeBrowsedContentID() {
        return beBrowsedContentID;
    }

    public void setBeBrowsedContentID(String beBrowsedContentID) {
        this.beBrowsedContentID = beBrowsedContentID;
    }

    public String getBeBrowsedContentType() {
        return beBrowsedContentType;
    }

    public void setBeBrowsedContentType(String beBrowsedContentType) {
        this.beBrowsedContentType = beBrowsedContentType;
    }

    @Override
    public String toString() {
        return "Log [uid=" + uid + ", logType=" + logType + ", logTime="
                + logTime + ", appType=" + appType + ", deviceID=" + deviceID
                + ", osType=" + osType + ", osVersion=" + osVersion
                + ", network=" + network + ", appVersion=" + appVersion
                + ", method=" + method + "]";
    }

}
