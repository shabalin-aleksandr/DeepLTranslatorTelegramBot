package com.telegrambot.deepl.command;

import com.telegrambot.deepl.service.SendMessageServiceInterface;
import org.telegram.telegrambots.meta.api.objects.Update;

public class HelpCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;

    public final static String HELP_MESSAGE = """
            ℹ️HELP MENUℹ️
            
            Here you can see commands what I can understand:
            
            /start - Display greeting message
            
            /help - Display info about acceptable commands
            
            ....
            """;

    public HelpCommand(SendMessageServiceInterface sendMessageServiceInterface) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
    }


    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();

        sendMessageServiceInterface.sendMessage(chatId, HELP_MESSAGE);
    }
}
