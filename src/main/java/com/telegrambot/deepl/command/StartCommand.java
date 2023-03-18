package com.telegrambot.deepl.command;

import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.UserService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StartCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final UserService userService;

    public final static String START_MESSAGE = """
            🔥Greetings🔥\s
            
            My name is DeepLTranslatorBot, as you may have understood from my name I am designed to translate text from one language to another.\s
            
            Write /help and you will find out what I can do.
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
