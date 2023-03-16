package com.telegrambot.deepl.service;

public interface SendMessageServiceInterface {
    void sendMessage(Long chatId, String message);
}
