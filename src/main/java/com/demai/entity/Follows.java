package com.demai.entity;

import java.io.Serializable;

public class Follows implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    private long tkey;

    private long uid;

    private long target;

    private int type;

    private String memo;

    private int star;

    private int contact;

    private int inAddressBook;

    private long time;

    public long getTkey() {
        return tkey;
    }

    public void setTkey(long tkey) {
        this.tkey = tkey;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getContact() {
        return contact;
    }

    public void setContact(int contact) {
        this.contact = contact;
    }

    public int getInAddressBook() {
        return inAddressBook;
    }

    public void setInAddressBook(int inAddressBook) {
        this.inAddressBook = inAddressBook;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
