package com.demai.dao;

import com.demai.entity.User;

import java.util.List;
import java.util.Map;

public interface IUserDao extends IBaseDao<User, Long> {

    public List<User> findByParam(Map<String, Object> params);

    public User findBySequence(long sequence);
}
