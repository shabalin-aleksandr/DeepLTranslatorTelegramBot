package com.telegrambot.deepl.service;

public interface TranslateMessageServiceInterface {
    void translateMessage(Long chatId, String message);
}
