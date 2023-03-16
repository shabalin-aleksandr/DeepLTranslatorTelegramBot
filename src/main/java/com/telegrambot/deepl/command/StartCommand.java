package com.telegrambot.deepl.command;

import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.UserService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StartCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final UserService userService;

    public final static String START_MESSAGE = """
            This bot is just a study project to try Spring Boot and Telegram API.

            You can execute commands from the main menu on the left or just type these commands:\s
            
           
           Type /start to see a welcome message
           
           Type /help to see this message again
           
           Type /mydata to see data stored about yourself
            """;

    public StartCommand(SendMessageServiceInterface sendMessageServiceInterface, UserService userService) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.userService = userService;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();

        userService.registerUser(update.getMessage());
        sendMessageServiceInterface.sendMessage(chatId, START_MESSAGE);
    }
}
