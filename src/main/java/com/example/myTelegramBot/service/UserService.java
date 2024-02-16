package com.example.myTelegramBot.service;

import com.example.myTelegramBot.domain.User;

import java.util.List;

public interface UserService {

    User save(User user);
    List<User> readAll();
    User updateById(User user, Long id);
    void deleteById(Long id);
}
