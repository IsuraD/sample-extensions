package com.cloud.computing.user.mgt.service;

import com.cloud.computing.user.mgt.dao.UserMgtDao;
import com.cloud.computing.user.mgt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserMgtService {

    @Autowired
    UserMgtDao userMgtDao;

    public List<User> getAll() {

        return userMgtDao.findAll();
    }

    public Optional<User> getById(long id) {

        return userMgtDao.findById(id);
    }

    public User save(User newUser) {

        return userMgtDao.save(newUser);
    }

    public void delete(long id) {

        userMgtDao.deleteById(id);
    }

}
