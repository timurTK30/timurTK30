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
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//my telegram bot
@Component
@Slf4j
public class MyBot extends TelegramLongPollingBot {

    List<Product> cart = new ArrayList<>();
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
                sendMassageToUser(chatId, msg, List.of("hi", "bye", "hahah"));
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            if (callbackQuery.getData().equalsIgnoreCase("hi")) {
                sendMessage(callbackQuery.getMessage().getChatId(), "ok");
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

    private InlineKeyboardMarkup createCustomKeyboard(List<String> buttons) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyBoard = new ArrayList<>();
        for (String s : buttons) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s);
            button.setCallbackData(s);

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(button);

            keyBoard.add(row1);
        }

        keyboardMarkup.setKeyboard(keyBoard);

        return keyboardMarkup;
    }

    private void startMenu(Long chatId) {
        sendMessage(chatId, "Добро пожаловать в моего первого бота!\n" +
                "команды: \n" +
                "/reg - позволает зарегестрироватся\n" +
                "/menu - меню покупок\n " +
                "/buy - добавить в корзину", null);
    }

    private void cart(Long chatId, String msg) {
        List<Product> productsFromMap = productService.readAll();
        Integer id = Integer.valueOf(msg.split(" ")[1]);
        Product product = productsFromMap.get(id - 1);
        cart.add(product);
        System.out.println(cart);
        sendMessage(chatId, cart.toString(), null);
    }

    private void productMenu(Long chatId) {
        List<Product> products = productService.readAll();
        StringBuilder menuText = new StringBuilder("Выберите товар для покупки\n\n");
        int index = 1;
        for (Product p : products) {
            menuText.append(index).
                    append(". ").append(p.getName()).
                    append(" цена: ").append(p.getPrice()).append("\n");
            index++;
        }
        sendMessage(chatId, menuText.toString(), null);
    }

    private void registerUser(Update update, String text) {
        String[] words = text.split(" ");
        String emeil = words[1];
        Long chatId = update.getMessage().getChatId();
        if (!isUserExist(chatId)) {
            String username = update.getMessage().getFrom().getUserName();
            userServiceimpl.save(
                    User.builder()
                            .chatId(chatId)
                            .username(username)
                            .email(emeil)
                            .build()
            );
        } else {
            sendMessage(chatId, "Вы уже зарегестриривоини", null);
        }


    private void sendMessage(Long chatId, String msg, List<String> buttons) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);

        if (buttons != null) {
            InlineKeyboardMarkup keyboardMarkup = createCustomKeyboard(buttons);
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
        if (userById.isEmpty()) {
            return false;
        } else {
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
