package com.telegrambot.deepl.command;

import com.telegrambot.deepl.service.SendMessageServiceInterface;
import org.telegram.telegrambots.meta.api.objects.Update;

public class WrongCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;

    public final static String WRONG_MESSAGE = """
            I do not understand you.🥹\s
            
            Please type a command starting with "/" or write /help to see what I understand.
            """;

    public WrongCommand(SendMessageServiceInterface sendMessageServiceInterface) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();

        sendMessageServiceInterface.sendMessage(chatId, WRONG_MESSAGE);

    }
}
