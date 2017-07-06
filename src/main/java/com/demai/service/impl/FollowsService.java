package com.demai.service.impl;

import com.demai.dao.IFollowsDao;
import com.demai.datasource.DataSourceSwitch;
import com.demai.entity.Follows;
import com.demai.service.IFollowsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dear on 16/4/22.
 */

@Service
public class FollowsService implements IFollowsService {

    private static final Logger logger = LoggerFactory.getLogger(FollowsService.class);

    @Resource
    IFollowsDao followsDao;

    /**
     * 是否是双向好友
     *
     * @param uid,target
     * @return
     */
    @Override
    public int findSecond(long uid, long target) {
        DataSourceSwitch.setDataSourceType("relationDataSource");
        int result = 0;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("uid", uid);
            params.put("target", target);
            result = followsDao.findSecond(params);
        } catch (Exception e) {
            logger.error("findSecond error", e);
        }
        return result;
    }

    @Override
    public Map<Long, Map<String, String>> findFriends(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("relationDataSource");
        Map<Long, Map<String, String>> result = new ConcurrentHashMap<>();
        Map<String, String> tempResult = new ConcurrentHashMap<>();
        try {
            List<Follows> list = followsDao.findFriends(params);
            if (list != null && list.size() > 0) {
                for (Follows f : list) {
                    if (result.containsKey(f.getUid())) {
                        tempResult = result.get(f.getUid());
                    } else {
                        tempResult = new ConcurrentHashMap<>();
                    }
                    if (!tempResult.containsKey(f.getTarget())) {
                        String key = f.getTarget() + "";
                        tempResult.put(key, key);
                        result.put(f.getUid(), tempResult);
                    }

                }
            }
        } catch (Exception e) {
            result = new ConcurrentHashMap<>();
            logger.error("findFriends error", e);
        }
        return result;
    }

    @Override
    public Map<String, String> findFriendsByUid(Long uid) {
        DataSourceSwitch.setDataSourceType("relationDataSource");
        Map<String, String> result = new ConcurrentHashMap<>();
        try {
            Map<String, Object> params = new HashMap<>();
            List<Long> uids = new ArrayList<>();
            uids.add(uid);
            params.put("uids",uids);
            List<Follows> list = followsDao.findFriends(params);
            if (list != null && list.size() > 0) {
                for (Follows f : list) {
                    String key = "" + f.getTarget();
                    result.put(key, key);
                }
            }
        } catch (Exception e) {
            result = new ConcurrentHashMap<>();
            logger.error("findFriends error", e);
        }
        return result;
    }
}
