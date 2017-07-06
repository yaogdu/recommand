package com.demai.dao.impl;

import com.demai.dao.IFollowsDao;
import com.demai.entity.Follows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by dear on 16/4/22.
 */
@Service
public class FollowsDao extends BaseDao<Follows, Long> implements IFollowsDao {

    private final String namespace = Follows.class.getName();

    @Override
    public int findSecond(Map<String, Object> params) {
        return (Integer) sqlSession.selectOne(namespace + ".findSecond", params);
    }

    @Override
    public List<Follows> findFriends(Map<String, Object> params) {
        return sqlSession.selectList(namespace + ".findFriends", params);
    }

    /**
     * 查询符合条件的纪录总条数
     *
     * @param follows@return
     */
    @Override
    public void update(Follows follows) {

    }
}
