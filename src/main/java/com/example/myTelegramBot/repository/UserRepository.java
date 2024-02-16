package com.example.myTelegramBot.repository;

import com.example.myTelegramBot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {


}
