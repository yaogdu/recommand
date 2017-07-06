package com.demai.service.impl;

import com.demai.dao.IUserDao;
import com.demai.datasource.DataSourceSwitch;
import com.demai.entity.User;
import com.demai.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("mysqlUserService")
public class UserService implements IUserService {

    @Autowired
    IUserDao userDao;

    @Override
    public User findById(Long id) {
        DataSourceSwitch.setDataSourceType("userDataSource");
        return userDao.findById(id);
    }

    @Override
    public List<User> findByParam(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("userDataSource");
        return userDao.findByParam(params);
    }

    @Override
    public Map<Long, User> findByMap(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("userDataSource");
        Map<Long, User> map = new ConcurrentHashMap<>();

        List<User> users = userDao.findByParam(params);
        if (users != null && users.size() > 0) {
            for (User user : users) {
                map.put(user.getTkey(), user);
            }
        }
        return map;
    }

    @Override
    public User findBySequence(long sequence) {
        DataSourceSwitch.setDataSourceType("userDataSource");
        return userDao.findBySequence(sequence);
    }

}
