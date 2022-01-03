package com.cloud.computing.user.mgt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class UserManagementApplication {

    public static void main(final String[] args) {

        SpringApplication.run(UserManagementApplication.class, args);
    }

}
