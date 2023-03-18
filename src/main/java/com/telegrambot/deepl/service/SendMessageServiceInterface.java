package com.telegrambot.deepl.service;

import java.util.List;

public interface SendMessageServiceInterface {
    void sendMessage(Long chatId, String message);

    void sendMessage(Long chatId, List<String> message);
}
