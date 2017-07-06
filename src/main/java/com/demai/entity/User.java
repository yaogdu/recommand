package com.demai.entity;

import java.io.Serializable;

public class User implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private long tkey;
    
    private String identifier;//identifier in db 
    
    private int single;
    
    private String otherMobile;
    
    public String getOtherMobile() {
        return otherMobile;
    }

    public void setOtherMobile(String otherMobile) {
        this.otherMobile = otherMobile;
    }
    
    private int coverPress;

    public int getCoverPress() {
        return coverPress;
    }

    public void setCoverPress(int coverPress) {
        this.coverPress = coverPress;
    }

    private int pattern;
    
    private String cpWebsite;
    
    private String cpAddress;

    private String cpEmail;
    
    private String wxNumber;
    
    private String tag;
    
    private String badge;
    
    private int confine;
    
    private String source;
    
    private String showPhoto;
    
    private String cover;
    
    private int app;
    
    private String account;
    
    private int mobileState;
    
    private int sequence;
    
    
    private String email;
    
    private String mobile;
    
    private String name;
    
    private String nick;
    
    private int sex;
    
    private char[] psw;
    
    private int msgflag;
    
    private int flag;//db存的二进制，需要改成十进制
    
    private int icon;
    
    private int rank;
    
    private String openPlat;
    
    private int position;
    
    private long time;
    
    private long badgeTime;
    
    private int integrity;
    
    private int point;

    public long getTkey() {
        return tkey;
    }

    public void setTkey(long tkey) {
        this.tkey = tkey;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getSingle() {
        return single;
    }

    public void setSingle(int single) {
        this.single = single;
    }

    public int getPattern() {
        return pattern;
    }

    public void setPattern(int pattern) {
        this.pattern = pattern;
    }

    public String getCpWebsite() {
        return cpWebsite;
    }

    public void setCpWebsite(String cpWebsite) {
        this.cpWebsite = cpWebsite;
    }

    public String getCpAddress() {
        return cpAddress;
    }

    public void setCpAddress(String cpAddress) {
        this.cpAddress = cpAddress;
    }

    public String getCpEmail() {
        return cpEmail;
    }

    public void setCpEmail(String cpEmail) {
        this.cpEmail = cpEmail;
    }

    public String getWxNumber() {
        return wxNumber;
    }

    public void setWxNumber(String wxNumber) {
        this.wxNumber = wxNumber;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public int getConfine() {
        return confine;
    }

    public void setConfine(int confine) {
        this.confine = confine;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getShowPhoto() {
        return showPhoto;
    }

    public void setShowPhoto(String showPhoto) {
        this.showPhoto = showPhoto;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getApp() {
        return app;
    }

    public void setApp(int app) {
        this.app = app;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getMobileState() {
        return mobileState;
    }

    public void setMobileState(int mobileState) {
        this.mobileState = mobileState;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public char[] getPsw() {
        return psw;
    }

    public void setPsw(char[] psw) {
        this.psw = psw;
    }

    public int getMsgflag() {
        return msgflag;
    }

    public void setMsgflag(int msgflag) {
        this.msgflag = msgflag;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getOpenPlat() {
        return openPlat;
    }

    public void setOpenPlat(String openPlat) {
        this.openPlat = openPlat;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getBadgeTime() {
        return badgeTime;
    }

    public void setBadgetime(long badgeTime) {
        this.badgeTime = badgeTime;
    }

    public int getIntegrity() {
        return integrity;
    }

    public void setIntegrity(int integrity) {
        this.integrity = integrity;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }
    
    
}
