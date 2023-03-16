package com.telegrambot.deepl.command;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandInterface {

    /**
     * Method, which is executing command.
     */
    void execute(Update update);
}
