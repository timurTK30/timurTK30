package com.example.myTelegramBot.service.serviceImpl;

import com.example.myTelegramBot.domain.User;
import com.example.myTelegramBot.repository.UserRepository;
import com.example.myTelegramBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> readAll() {
        return userRepository.findAll();
    }

    @Override
    public User updateById(User user, Long id) {
        if (id != null) {
            user.setId(id);
            return userRepository.save(user);
        }else {
            throw new RuntimeException("user not found with id: " + id + ".");
        }
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
