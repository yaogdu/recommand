package com.demai.dao;

import com.demai.entity.Follows;

import java.util.List;
import java.util.Map;

/**
 * Created by dear on 16/4/22.
 */
public interface IFollowsDao extends IBaseDao<Follows, Long> {
    public int findSecond(Map<String, Object> params);

    public List<Follows> findFriends(Map<String, Object> params);
}
