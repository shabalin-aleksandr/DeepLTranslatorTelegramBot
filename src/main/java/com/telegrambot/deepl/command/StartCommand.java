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

import com.telegrambot.deepl.bot.DeepLTelegramBot;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StartCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final UserService userService;
    private final DeepLTelegramBot deeplBot;

    public final static String START_MESSAGE = """
            🔥Greetings🔥\s
            
            My name is DeepLTranslatorBot, as you may have understood from my name I am designed to translate text from one language to another.\s
            
            Write /help and you will find out what I can do.
            """;

    public StartCommand(SendMessageServiceInterface sendMessageServiceInterface,
                        UserService userService,
                        DeepLTelegramBot deeplBot) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.userService = userService;
        this.deeplBot = deeplBot;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();

        userService.registerUser(update.getMessage());
        setupBotMenu();
        sendMessageServiceInterface.sendMessage(chatId, START_MESSAGE);
    }

    private void setupBotMenu() {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/start", "Get a welcome message"));
        botCommands.add(new BotCommand("/help", "Info about commands"));
        botCommands.add(new BotCommand("/setlanguages", "Language selection"));
        botCommands.add(new BotCommand("/languages", "List of languages"));
        botCommands.add(new BotCommand("/deletemydata", "Delete your account"));

        try {
            deeplBot.execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
