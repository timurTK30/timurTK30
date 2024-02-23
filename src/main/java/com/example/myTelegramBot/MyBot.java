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

import java.util.*;

//my telegram bot
@Component
@Slf4j
public class MyBot extends TelegramLongPollingBot {

    private BotConfig botConfig;
    private UserServiceImpl userServiceimpl;

    private Map<String, Integer> products = new HashMap<>();


    @Autowired
    public MyBot(BotConfig botConfig, UserServiceImpl userServiceimpl) {
        this.botConfig = botConfig;
        this.userServiceimpl = userServiceimpl;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage().hasText() && update.hasMessage()){
            String msg = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if(msg.startsWith("/reg")){
                registerUser(update, msg);
            }else if (msg.startsWith("/menu")){
                productMenu(chatId);
            }else if (msg.startsWith("/start")){
                startMenu(chatId);
            }else if (msg.startsWith("/buy")){
                cart(chatId, msg);
            }
        }
    }

    private void startMenu(Long chatId){
        sendMessage(chatId, "Добро пожаловать в моего первого бота!\n" +
                "команды: \n" +
                "/reg - позволает зарегестрироватся\n" +
                "/menu - будущие меню покупок\n +" +
                "/buy - добавить в корзину");
    }


    List<String> cart = new ArrayList<>();

    private void cart(Long chatId, String msg){
        List<String> productsFromMap = new ArrayList<>();
        products.put("недаделаный плюш ТК", 50);
        products.put("аптека первой помощи", 20);
        products.put("маркер", 4);
        for (Map.Entry<String, Integer> entry : products.entrySet()){
            productsFromMap.add(entry.getKey());
        }
        Integer id = Integer.valueOf(msg.split(" ")[1]);
        String product = productsFromMap.get(id - 1);
        cart.add(product);
        //products.entrySet().stream()
              //  .filter(p -> p.getKey().equalsIgnoreCase(product));
        System.out.println(cart);
        sendMessage(chatId, cart.toString());
    }

    private void productMenu( Long chatId){
        products.put("недаделаный плюш ТК", 50);
        products.put("аптека первой помощи", 20);
        products.put("маркер", 4);

        StringBuilder menuText = new StringBuilder("Выберите товар для покупки\n\n");
        int index = 1;
        for (Map.Entry<String, Integer> entry : products.entrySet()){
            menuText.append(index).
                    append(". ").append(entry.getKey()).
                    append(" цена: ").append(entry.getValue()).append("\n");
            index++;
        }
        sendMessage(chatId, menuText.toString());
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
