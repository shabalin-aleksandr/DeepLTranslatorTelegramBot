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

package com.telegrambot.deepl.command;

import com.telegrambot.deepl.config.ChatIdHolder;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class DeleteCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final UserService userService;

    private final static String DELETE_MESSAGE_EN = """
            ✅Your data were successfully deleted✅
            
            If you want to go back, just type /start again.
            
            To clear your chat history with this bot, please follow these steps:
            1. Tap on the bot's name at the top of the chat.
            2. Tap on 'Clear Messages' (on mobile) or 'Clear Chat History' (on desktop).
            3. Confirm the action.
            """;
    private final static String DELETE_MESSAGE_RU = """
            ✅Ваши данные были успешно удалены✅
            
            Если вы захотите вернуться назад, просто введите /start еще раз.

            Чтобы очистить историю чата с этим ботом, выполните следующие действия:
            1. Нажмите на имя бота в верхней части чата.
            2. Нажмите на "Удалить переписку" (на мобильном) или "Очистить историю" (на настольном).
            3. Подтвердите действие.
            """;

    public DeleteCommand(SendMessageServiceInterface sendMessageServiceInterface, UserService userService) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.userService = userService;
    }

    @Override
    public void execute(Update update) {
        if (update.hasCallbackQuery()) {
            try {
                handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
        } else if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();

            userService.removeUserLanguage(chatId);
            userService.removeUserLanguagePair(chatId);
            ChatIdHolder chatIdHolder = new ChatIdHolder(chatId);
            userService.deleteUser(chatIdHolder);

            setTranslateButtonDelete(chatId);
        }
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        CommandUtility.handleTranslateCallbackQuery(sendMessageServiceInterface,
                "translate_russian_delete",
                callbackQuery,
                DELETE_MESSAGE_RU);
    }

    private void setTranslateButtonDelete(Long chatId) {
        CommandUtility.setTranslateButton(sendMessageServiceInterface,
                "Перевести на русский язык 🇷🇺",
                "translate_russian_delete",
                chatId,
                DELETE_MESSAGE_EN);
    }
}
