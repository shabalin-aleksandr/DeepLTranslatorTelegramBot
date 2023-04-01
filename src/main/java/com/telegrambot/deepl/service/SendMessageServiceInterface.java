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

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public interface SendMessageServiceInterface {
    void sendMessage(Long chatId, String message);
    void sendMessage(Long chatId, List<String> message);
    void sendMessage(SendMessage message) throws InterruptedException, TelegramApiException;
    void editMessage(EditMessageText editMessageText) throws TelegramApiException;
    void deleteMessage(DeleteMessage deleteMessage) throws TelegramApiException;
    void answerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) throws TelegramApiException;
}
