package com.cloud.computing.user.mgt.dao;

import com.cloud.computing.user.mgt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMgtDao extends JpaRepository<User, Long> {

}
