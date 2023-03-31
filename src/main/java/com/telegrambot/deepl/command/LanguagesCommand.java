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

import com.telegrambot.deepl.service.SendMessageServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class LanguagesCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;

    private final static String LIST_OF_LANGUAGES_MESSAGE_EN = """
            🇺🇸🇩🇪🇨🇿🇪🇸🇫🇷🇮🇹🇷🇺🇺🇦
            Here is a list of available languages:
            
            🇺🇸 - English
            🇩🇪 - German
            🇨🇿 - Czech
            🇪🇸 - Spanish
            🇫🇷 - French
            🇮🇹 - Italian
            🇷🇺 - Russian
            🇺🇦 - Ukrainian
            
            You can use all of these languages in auto-define language mode with /translate.\s
            
            You can also use these languages with the command /set_languages.
            """;
    private final static String LIST_OF_LANGUAGES_MESSAGE_RU = """
            🇺🇸🇩🇪🇨🇿🇪🇸🇫🇷🇮🇹🇷🇺🇺🇦
            Вот список доступных языков:

            🇺🇸 - Английский
            🇩🇪 - Немецкий
            🇨🇿 - Чешский
            🇪🇸 - Испанский
            🇫🇷 - Французский
            🇮🇹 - Итальянский
            🇷🇺 - Русский
            🇺🇦 - Украинский

            Вы можете использовать все эти языки в режиме автоматического определения языка с помощью команды /translate.\s

            Также эти языки доступны с помощью команды /set_languages.
            """;

    public LanguagesCommand(SendMessageServiceInterface sendMessageServiceInterface) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
    }

    @Override
    public void execute(Update update) throws InterruptedException {
        if (update.hasCallbackQuery()) {
            try {
                handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
        } else if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            setTranslationButtonLanguage(chatId);
        }

    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        CommandUtility.handleTranslateCallbackQuery(sendMessageServiceInterface,
                "translate_russian_lang",
                callbackQuery,
                LIST_OF_LANGUAGES_MESSAGE_RU);
    }

    private void setTranslationButtonLanguage(Long chatId) {
        CommandUtility.setTranslateButton(sendMessageServiceInterface,
                "Перевести на русский язык 🇷🇺",
                "translate_russian_lang",
                chatId,
                LIST_OF_LANGUAGES_MESSAGE_EN);
    }
}
