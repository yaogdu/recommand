package com.demai.entity;

import com.demai.common.bean.Feed;
import com.demai.common.bean.User;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dear on 16/2/26.
 */
public class ResultMapping implements Serializable {

    private Feed feed;

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    private List<User> users;

}
