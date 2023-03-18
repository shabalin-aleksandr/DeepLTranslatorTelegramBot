package com.telegrambot.deepl.command;

import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.UserService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class DeleteCommand implements CommandInterface{

    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final UserService userService;

    public final static String DELETE_MESSAGE = """
            ✅Your data were successfully deleted.✅
            
            If you want to go back, just type /start again.
            """;

    public DeleteCommand(SendMessageServiceInterface sendMessageServiceInterface, UserService userService) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.userService = userService;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();

        userService.deleteUser(update.getMessage());
        sendMessageServiceInterface.sendMessage(chatId, DELETE_MESSAGE);
    }
}
