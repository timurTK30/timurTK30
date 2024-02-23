package com.example.myTelegramBot.repository;

import com.example.myTelegramBot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.chatId =:chatId")
    User findUserByChatId(@Param("chatId") Long chatId);
}
