package com.telegrambot.deepl.service;

import com.deepl.api.*;
import com.telegrambot.deepl.bot.DeepLTelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@PropertySource("application.properties")
public class TranslateMessageService implements TranslateMessageServiceInterface {

    Translator translator;

//    @Value("${deepl.token}")
//    private String AUTH_KEY;

    public TranslateMessageService(DeepLTelegramBot deepLBot) {
    }

    @Override
    public TextResult translateMessage(String message) {
        String authKey = "6b6e8175-9411-a0fb-35ca-6a1797502679:fx";
        translator = new Translator(authKey);

        TextResult result = null;
        try {
            result = translator.translateText(message, "en", "ru");
        } catch (DeepLException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return result;
    }
}
