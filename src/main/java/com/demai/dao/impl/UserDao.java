package com.demai.dao.impl;

import com.demai.dao.IUserDao;
import com.demai.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserDao extends BaseDao<User, Long> implements IUserDao {

    private final String namespace = User.class.getName();

    @Override
    public void update(User t) {
        super.update("update", t);
    }

    @Override
    public List<User> findByParam(Map<String, Object> params) {
        return sqlSession.selectList(namespace + ".findByParam", params);
    }

    @Override
    public User findBySequence(long sequence) {
        return sqlSession.selectOne(namespace + ".findBySequence",sequence);
    }
}
