package com.telegrambot.deepl.service;

import com.deepl.api.TextResult;

public interface TranslateMessageServiceInterface {
    TextResult translateMessage(String message);
}
