package com.example.myTelegramBot.config;

import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BotConfig {

    private final String botName = "rb_games_bot";
    private final String botToken = "6734653449:AAHrXTc_UedtXdCsDU-q1VFUwVJj_M7w33g";
}
