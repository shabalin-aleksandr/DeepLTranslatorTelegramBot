package com.telegrambot.deepl.command;

import com.telegrambot.deepl.service.SendMessageService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UnknownCommand implements CommandInterface {

    public static final String UNKNOWN_MESSAGE = """
    I don't understand this command 🥹, you can write /help to see what I understand.
    """;

    private final SendMessageService sendMessageService;

    public UnknownCommand(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;
    }

    @Override
    public void execute(Update update) {
        sendMessageService.sendMessage(update.getMessage().getChatId(), UNKNOWN_MESSAGE);
    }
}
