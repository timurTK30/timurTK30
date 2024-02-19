package com.example.myTelegramBot;

import com.example.myTelegramBot.config.BotConfig;
import com.example.myTelegramBot.domain.User;
import com.example.myTelegramBot.service.UserService;
import com.example.myTelegramBot.service.serviceImpl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

//my telegram bot
@Component
@Slf4j
public class MyBot extends TelegramLongPollingBot {

    private BotConfig botConfig;
    private UserServiceImpl userServiceimpl;

    @Autowired
    public MyBot(BotConfig botConfig, UserServiceImpl userServiceimpl) {
        this.botConfig = botConfig;
        this.userServiceimpl = userServiceimpl;
    }

    @Override
    public void onUpdateReceived(Update update) {
        sendMessage(update.getMessage().getChatId(), update.getMessage().getText());
        if(update.getMessage().getText().startsWith("/reg")){
            registerUser(update, update.getMessage().getText());
        }
    }

    private void gameMenu(Update update, String message, Long chatId){
        if(message.startsWith("/menu")){
            sendMessage(chatId, "недаделаный плюш ТК, цена: 50\n" +
                    "аптека первой помощи, цена: 20\n" +
                    "маркер, цена: 4");
        }
    }

    private void registerUser(Update update, String text) {
        String[] words = text.split(" ");
        String emeil = words[1];
        Long chatId = update.getMessage().getChatId();
        if(!isUserExist(chatId)){
            String username = update.getMessage().getFrom().getUserName();
            userServiceimpl.save(
                    User.builder()
                            .chatId(chatId)
                            .username(username)
                            .email(emeil)
                            .build()
            );
        }else {
            sendMessage(chatId, "Вы уже зарегестриривоини");
        }

    }

    public void sendMessage(Long chatId, String msg){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean isUserExist(Long id){
        Optional<User> userById = userServiceimpl.findUserByChatId(id);
        if(userById.isEmpty()){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }
}
