package com.example.myTelegramBot;

import com.example.myTelegramBot.config.BotConfig;
import com.example.myTelegramBot.domain.Product;
import com.example.myTelegramBot.domain.User;
import com.example.myTelegramBot.service.serviceImpl.ProductServiceImpl;
import com.example.myTelegramBot.service.serviceImpl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//my telegram bot
@Component
@Slf4j
public class MyBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserServiceImpl userServiceimpl;
    private final ProductServiceImpl productService;
    List<Product> cart = new ArrayList<>();


    @Autowired
    public MyBot(BotConfig botConfig, UserServiceImpl userServiceimpl, ProductServiceImpl productService) {
        this.botConfig = botConfig;
        this.userServiceimpl = userServiceimpl;
        this.productService = productService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String msg = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (msg.startsWith("/reg")) {
                registerUser(update, msg);
            } else if (msg.startsWith("/menu")) {
                productMenu(chatId);
            } else if (msg.startsWith("/start")) {
                startMenu(chatId);
            } else if (msg.startsWith("/buy")) {
                cart(chatId, msg);
            } else if (msg.startsWith("/hi")) {
                sendMessage(chatId, msg, List.of("hi", "bye", "hahah"), 2);
            } else if (msg.startsWith("/sendPhoto")) {
                sendPhotoToUser(chatId, "C:\\Users\\tverd\\Videos\\Roblox\\images.jpg");
            } else if (msg.startsWith("/sendVideo")) {
                sendVideo(chatId, "C:\\Users\\tverd\\Videos\\Roblox\\robloxapp-20240302-1712111.wmv");
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            if (callbackQuery.getData().equalsIgnoreCase("hi")) {
                sendMessage(callbackQuery.getMessage().getChatId(), "ok", null, null);
            }
        }
    }

    private void sendVideo(Long chatId, String url) {
        File video = new File(url);
        InputFile inputFile = new InputFile(video);
        SendVideoNote sendVideo = new SendVideoNote();
        sendVideo.setChatId(chatId);
        sendVideo.setVideoNote(inputFile);
        //sendVideo.setSupportsStreaming(true);
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPhotoToUser(Long chatId, String url) {
        File photo = new File(url);
        InputFile inputFile = new InputFile(photo);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(inputFile);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private InlineKeyboardMarkup createCustomKeyboard(List<String> buttonText, int rows) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        int buttonsPerRow = buttonText.size() / rows + buttonText.size() % rows;
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (String s : buttonText) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s);
            button.setCallbackData(s);

            row.add(button);
            if (row.size() == buttonsPerRow || button.equals(buttonText.get(buttonText.size() - 1))) {
                keyboard.add(row);
                row = new ArrayList<>();
            }
        }

        if (buttonText.size() % rows == 1) {
            List<InlineKeyboardButton> buttonRow = new ArrayList<>();
            for (int i = 0; i < buttonText.size() % rows; i++) {
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(buttonText.get(buttonText.size() - buttonText.size() % rows + i));
                inlineKeyboardButton.setCallbackData(buttonText.get(buttonText.size() - buttonText.size() % rows + i));
                buttonRow.add(inlineKeyboardButton);
            }

            keyboard.add(buttonRow);
        }

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    private void startMenu(Long chatId) {
        sendMessage(chatId, "Добро пожаловать в моего первого бота!\n" + "команды: \n" + "/reg - позволает зарегестрироватся\n" + "/menu - меню покупок\n " + "/buy - добавить в корзину", null, null);
    }

    private void cart(Long chatId, String msg) {
        List<Product> productsFromMap = productService.readAll();
        Integer id = Integer.valueOf(msg.split(" ")[1]);
        Product product = productsFromMap.get(id - 1);
        cart.add(product);
        System.out.println(cart);
        sendMessage(chatId, cart.toString(), null, null);
    }

    private void productMenu(Long chatId) {
        List<Product> products = productService.readAll();
        StringBuilder menuText = new StringBuilder("Выберите товар для покупки\n\n");
        int index = 1;
        for (Product p : products) {
            menuText.append(index).append(". ").append(p.getName()).append(" цена: ").append(p.getPrice()).append("\n");
            index++;
        }
        sendMessage(chatId, menuText.toString(), null, null);
    }

    private void registerUser(Update update, String text) {
        String[] words = text.split(" ");
        String emeil = words[1];
        Long chatId = update.getMessage().getChatId();
        if (!isUserExist(chatId)) {
            String username = update.getMessage().getFrom().getUserName();
            userServiceimpl.save(User.builder().chatId(chatId).username(username).email(emeil).build());
        } else {
            sendMessage(chatId, "Вы уже зарегестриривоини", null, null);
        }
    }

    private void sendMessage(Long chatId, String msg, List<String> buttons, Integer rows) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);

        if (buttons != null) {
            InlineKeyboardMarkup keyboardMarkup = createCustomKeyboard(buttons, rows);
            sendMessage.setReplyMarkup(keyboardMarkup);
        }

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean isUserExist(Long id) {
        Optional<User> userById = userServiceimpl.findUserByChatId(id);
        return userById.isPresent();
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
