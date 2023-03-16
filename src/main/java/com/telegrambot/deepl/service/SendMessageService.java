package com.telegrambot.deepl.service;

import com.telegrambot.deepl.bot.DeepLTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class SendMessageService implements SendMessageServiceInterface{

    private final DeepLTelegramBot deepLBot;

    @Autowired
    public SendMessageService(DeepLTelegramBot deepLBot) {
        this.deepLBot = deepLBot;
    }

    @Override
    public void sendMessage(Long chatId, String message) {
        if (isBlank(message)) return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.enableHtml(true);
        sendMessage.setText(message);

        try {
            deepLBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
