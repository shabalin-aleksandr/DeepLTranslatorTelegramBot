package com.telegrambot.deepl.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandInterface {

    /**
     * Method, which is executing command.
     */
    void execute(Update update) throws InterruptedException;
}
