/*
- Copyright 2023 Aleksandr Shabalin
-
- Licensed under the Apache License, Version 2.0 (the "License");
- you may not use this file except in compliance with the License.
- You may obtain a copy of the License at
-
- `<http://www.apache.org/licenses/LICENSE-2.0>`
-
- Unless required by applicable law or agreed to in writing, software
- distributed under the License is distributed on an "AS IS" BASIS,
- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
- See the License for the specific language governing permissions and
- limitations under the License.
*/

package com.telegrambot.deepl.service;

import com.telegrambot.deepl.bot.DeepLTelegramBot;
import com.telegrambot.deepl.config.ChatIdHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
public class SendMessageService implements SendMessageServiceInterface {

    private final DeepLTelegramBot deepLBot;
    private final UserService userService;

    @Autowired
    public SendMessageService(DeepLTelegramBot deepLBot, UserService userService) {
        this.deepLBot = deepLBot;
        this.userService = userService;
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
            log.error("Error occurred: " + e.getMessage());
            if (e.getMessage().contains("chat not found")) {
                log.warn("Failed to send message to chat ID: " + chatId);
                ChatIdHolder chatIdHolder = new ChatIdHolder(chatId);
                userService.deleteUser(chatIdHolder);
            }
        }
    }

    @Override
    public void sendMessage(Long chatId, List<String> messages) {
        if (isEmpty(messages)) return;
        messages.forEach(m -> sendMessage(chatId, m));
    }

    public void sendMessage(SendMessage message) throws InterruptedException, TelegramApiException {
        deepLBot.execute(message);
    }

    @Override
    public void editMessage(EditMessageText editMessageText) throws TelegramApiException {
        deepLBot.execute(editMessageText);
    }

    @Override
    public void answerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) throws TelegramApiException {
        deepLBot.execute(answerCallbackQuery);
    }
}
