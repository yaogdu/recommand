package com.demai.service;


import java.util.Map;


public interface IFollowsService {

    public int findSecond(long uid, long target);

    public Map<Long, Map<String, String>> findFriends(Map<String, Object> params);

    public Map<String, String> findFriendsByUid(Long uid);
}
