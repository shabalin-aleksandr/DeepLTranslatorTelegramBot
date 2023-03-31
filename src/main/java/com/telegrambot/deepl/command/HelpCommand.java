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
public class HelpCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;

    private final static String HELP_MESSAGE_EN = """
            ℹ️ HELP MENU ℹ️
            
            🔻 Here you can see commands what I can understand 🔻
            
            ❇️ /start - Display greeting message
            
            🧠 /help - Display info about acceptable commands
            
            🌐 /translate - This command will automatically detect the language of the message you have sent and translate it into the language of your choice
            
            👀 /set_languages - The command displays a menu for selecting a pair of languages for translation
                        
            📙 /languages - To see a list of available languages that the bot understands
            
            💭 /support - View bot administrator contacts
            
            ⛔️ /delete_my_data - This command will delete all data about you, as well as terminate the bot
            
            """;
    private final static String HELP_MESSAGE_RU = """
            ℹ️ ПОМОЩЬ ℹ️
            
            🔻 Здесь вы можете увидеть команды, которые я понимаю🔻
            
            ❇️ /start - Приветственное сообщение
            
            🧠 /help - Показать информацию о доступных командах
            
            🌐 /translate - Эта команда автоматически определит язык отправленного вами сообщения и переведет его на выбранный вами язык
                        
            👀 /set_languages - Команда отображает меню для выбора пары языков для перевода
            
            📙 /languages - Посмотреть список доступных языков, которые понимает бот
            
            💭 /support - Просмотр контактов администратора бота
            
            ⛔️ /delete_my_data - Эта команда удалит все данные о вас, а также завершит работу бота
            """;

    public HelpCommand(SendMessageServiceInterface sendMessageServiceInterface) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
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
            setTranslateButtonHelp(chatId);
        }
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        CommandUtility.handleTranslateCallbackQuery(sendMessageServiceInterface,
                "translate_russian_help",
                callbackQuery,
                HELP_MESSAGE_RU);
    }

    private void setTranslateButtonHelp(Long chatId) {
        CommandUtility.setTranslateButton(sendMessageServiceInterface,
                "Перевести на русский язык 🇷🇺",
                "translate_russian_help",
                chatId,
                HELP_MESSAGE_EN);
    }
}
