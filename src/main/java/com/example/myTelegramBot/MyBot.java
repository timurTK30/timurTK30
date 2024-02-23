package com.example.myTelegramBot;

import com.example.myTelegramBot.config.BotConfig;
import com.example.myTelegramBot.domain.Product;
import com.example.myTelegramBot.domain.User;
import com.example.myTelegramBot.service.UserService;
import com.example.myTelegramBot.service.serviceImpl.ProductServiceImpl;
import com.example.myTelegramBot.service.serviceImpl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.security.auth.callback.Callback;
import java.util.*;

//my telegram bot
@Component
@Slf4j
public class MyBot extends TelegramLongPollingBot {

    private BotConfig botConfig;
    private UserServiceImpl userServiceimpl;

    private ProductServiceImpl productService;


    @Autowired
    public MyBot(BotConfig botConfig, UserServiceImpl userServiceimpl, ProductServiceImpl productService) {
        this.botConfig = botConfig;
        this.userServiceimpl = userServiceimpl;
        this.productService = productService;
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
            }else if(msg.startsWith("/hi")){
                try {
                    execute(
                            SendMessage.builder().chatId(chatId).text("Привет").
                                    replyMarkup(createHiKeyboard()).build()
                    );
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            if (callbackQuery.getData().equalsIgnoreCase("hi")){
                sendMessage(callbackQuery.getFrom().getId(), "ok");
            }
        }
    }

    private InlineKeyboardMarkup createHiKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(new ArrayList<>());
        InlineKeyboardButton hiButton = new InlineKeyboardButton();
        hiButton.setText("hi");
        hiButton.setCallbackData("hi");

        InlineKeyboardButton byeButton = new InlineKeyboardButton();
        byeButton.setText("bye");
        byeButton.setCallbackData("bye");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(hiButton);
        row1.add(byeButton);

        keyboardMarkup.getKeyboard().add(row1);

        return keyboardMarkup;
    }

    private void startMenu(Long chatId){
        sendMessage(chatId, "Добро пожаловать в моего первого бота!\n" +
                "команды: \n" +
                "/reg - позволает зарегестрироватся\n" +
                "/menu - меню покупок\n " +
                "/buy - добавить в корзину");
    }

    List<Product> cart = new ArrayList<>();

    private void cart(Long chatId, String msg){
        List<Product> productsFromMap = productService.readAll();
        Integer id = Integer.valueOf(msg.split(" ")[1]);
        Product product = productsFromMap.get(id - 1);
        cart.add(product);
        System.out.println(cart);
        sendMessage(chatId, cart.toString());
    }

    private void productMenu( Long chatId){
        List<Product> products = productService.readAll();
        StringBuilder menuText = new StringBuilder("Выберите товар для покупки\n\n");
        int index = 1;
        for (Product p: products){
            menuText.append(index).
                    append(". ").append(p.getName()).
                    append(" цена: ").append(p.getPrice()).append("\n");
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
