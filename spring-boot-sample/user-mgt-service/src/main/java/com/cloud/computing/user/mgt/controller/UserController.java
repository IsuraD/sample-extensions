package com.cloud.computing.user.mgt.controller;

import com.cloud.computing.user.mgt.exception.RecordNotFoundException;
import com.cloud.computing.user.mgt.exception.ServerException;
import com.cloud.computing.user.mgt.model.User;
import com.cloud.computing.user.mgt.service.UserMgtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    UserMgtService userMgtService;

    @GetMapping("users")
    public ResponseEntity<List<User>> getAll() {

        return new ResponseEntity<>(userMgtService.getAll(), HttpStatus.OK);
    }

    @GetMapping("users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable final long id) {

        Optional<User> user = userMgtService.getById(id);
        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            throw new RecordNotFoundException();
        }
    }

    @PostMapping(path = "users",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> addUser(@RequestBody final User newUser) {

        User user = userMgtService.save(newUser);
        if (user == null) {
            throw new ServerException();
        } else {
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }
    }

    @PutMapping("users/{id}")
    public ResponseEntity<User> updateUser(@RequestBody final User updatedUser, @PathVariable final long id) {

        updatedUser.setId(id);
        User user = userMgtService.save(updatedUser);
        if (user == null) {
            throw new ServerException();
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    @DeleteMapping("users/{id}")
    public HttpStatus deleteUser(@PathVariable final long id) {

        try {
            userMgtService.delete(id);
            return HttpStatus.OK;
        } catch (Exception e) {
            throw new RecordNotFoundException();
        }
    }
}
