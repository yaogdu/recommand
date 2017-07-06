package com.demai.service;

import com.demai.entity.User;

import java.util.List;
import java.util.Map;

public interface IUserService {
    
    public User findById(Long id);

    public List<User> findByParam(Map<String, Object> params);

    public Map<Long,User> findByMap(Map<String, Object> params);

    public User findBySequence(long sequence);

}
