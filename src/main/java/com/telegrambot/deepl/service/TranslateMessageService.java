package com.telegrambot.deepl.service;

import com.deepl.api.*;
import com.telegrambot.deepl.bot.DeepLTelegramBot;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TranslateMessageService implements TranslateMessageServiceInterface {

    Translator translator;

    public TranslateMessageService(DeepLTelegramBot deepLBot) {
    }

    @Override
    public TextResult translateMessage(String message) {
        String authKey = "6b6e8175-9411-a0fb-35ca-6a1797502679:fx";
        translator = new Translator(authKey);

        TextResult result = null;
        try {
            result = translator.translateText(message, "en", "cs");
        } catch (DeepLException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return result;
    }
}
