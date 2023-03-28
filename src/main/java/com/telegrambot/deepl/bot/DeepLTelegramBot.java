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

package com.telegrambot.deepl.bot;

import com.telegrambot.deepl.command.CommandContainer;
import com.telegrambot.deepl.config.BotConfig;
import com.telegrambot.deepl.repository.UserRepositoryInterface;
import com.telegrambot.deepl.service.SendMessageService;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.TranslateMessageService;
import com.telegrambot.deepl.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.telegrambot.deepl.command.CommandName.TRANSLATE;

@Slf4j
@Component
@Service
public class DeepLTelegramBot extends TelegramLongPollingBot {

    public static String COMMAND_START = "/";
    final BotConfig config;
    private final CommandContainer commandContainer;
    private final UserService userService;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    public DeepLTelegramBot(UserService userService, BotConfig config,
                            UserRepositoryInterface userRepositoryInterface) {
        this.userService = userService;
        this.config = config;
        this.commandContainer = new CommandContainer( new SendMessageService(this, userService),
                new TranslateMessageService(this),
                userService, userRepositoryInterface, config, this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            try {
                commandContainer.findCommand(TRANSLATE.getCommandName()).handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                log.error("Error handling callback query: ", e);
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText().trim();
            String username = update.getMessage().getFrom().getUserName();
            String firstName = update.getMessage().getChat().getFirstName();

            if (message.startsWith(COMMAND_START)) {
                String commandId = message.split(" ")[0].toLowerCase();
                boolean userExists = userService.isUserExists(chatId);

                if (!userExists && !commandId.equals("/start")) {
                    SendMessageServiceInterface sendMessageServiceInterface = applicationContext.getBean(SendMessageService.class);
                    sendMessageServiceInterface.sendMessage(chatId, "Please use /start to begin using the bot again.");
                } else {
                    try {
                        commandContainer.findCommand(commandId).execute(update);
                    } catch (InterruptedException e) {
                        log.error("Error executing command: " + commandId, e);
                    }
                    log.info("This was a response to the user: " +
                            firstName + "(" + username + ") to the command: " + message);
                }
            } else {
                try {
                    commandContainer.findCommand(TRANSLATE.getCommandName()).execute(update);
                } catch (InterruptedException e) {

                    log.error("Error executing TranslateCommand without a command: ", e);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }
}
