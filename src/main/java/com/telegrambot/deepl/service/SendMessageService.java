package com.telegrambot.deepl.service;

import com.telegrambot.deepl.bot.DeepLTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;


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

    @Override
    public void sendMessage(Long chatId, List<String> messages) {
        if (isEmpty(messages)) return;

        messages.forEach(m -> sendMessage(chatId, m));
    }
}
