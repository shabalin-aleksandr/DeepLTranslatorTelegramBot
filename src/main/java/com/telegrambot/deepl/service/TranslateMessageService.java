package com.telegrambot.deepl.service;

import com.deepl.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@PropertySource("application.properties")
public class TranslateMessageService implements TranslateMessageServiceInterface {

    Translator translator;

    @Value("${deepl.token}")
    private static String AUTH_KEY;

    @Override
    public void translateMessage(Long chatId, String message) {

    }
}
