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

import static com.telegrambot.deepl.command.CommandName.*;

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
        this.commandContainer = new CommandContainer(new SendMessageService(this, userService),
                new TranslateMessageService(this),
                userService, userRepositoryInterface, config, this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            processCallbackQuery(update);
        } else if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                processMessageUpdate(update);
            } else if (update.getMessage().isCommand()) {
                try {
                    commandContainer.findCommand(update.getMessage().getText()).execute(update);
                } catch (InterruptedException e) {
                    log.error("Error executing command: " + update.getMessage().getText(), e);
                }
            }
        }
    }

    private void processCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        String command = switch (callbackData) {
            case "translate_russian_start" -> START.getCommandName();
            case "translate_russian_help" -> HELP.getCommandName();
            case "translate_russian_lang" -> LANGUAGES.getCommandName();
            case "translate_russian_support" -> ADMIN_CONTACTS.getCommandName();
            case "translate_russian_delete" -> DELETE.getCommandName();
            case "translate_russian_admin" -> ADMIN_SEND_COMMAND.getCommandName();
            default -> null;
        };

        if (command != null) {
            handleCallbackWithCommand(update, command);
        } else {
            Long userId = update.getCallbackQuery().getFrom().getId();
            String lastCommand = userService.getLastCommandForUser(userId);
            log.warn(lastCommand);

            if (AUTO_TRANSLATE.getCommandName().equals(lastCommand)) {
                handleCallbackWithCommand(update, AUTO_TRANSLATE.getCommandName());
            } else if (SET_LANGUAGE.getCommandName().equals(lastCommand)) {
                handleCallbackWithCommand(update, SET_LANGUAGE.getCommandName());
            }
        }
    }

    private void handleCallbackWithCommand(Update update, String command) {
        try {
            commandContainer.findCommand(command).handleCallbackQuery(update.getCallbackQuery());
        } catch (TelegramApiException e) {
            log.error("Error handling callback query: ", e);
        }
    }

    private void processMessageUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText().trim();
        String username = update.getMessage().getFrom().getUserName();
        String firstName = update.getMessage().getChat().getFirstName();
        String commandId = getCommandIdFromMessage(message);

        if (commandId != null) {
            boolean userExists = userService.isUserExists(chatId);

            if (!userExists && !commandId.equals("/start")) {
                SendMessageServiceInterface sendMessageServiceInterface = applicationContext.getBean(SendMessageService.class);
                sendMessageServiceInterface.sendMessage(chatId, "Please use /start to begin using the bot again.");
            } else {
                executeCommandAndUpdateLog(update, commandId, firstName, username, message);
            }
        } else {
            handleNonCommandMessage(update);
        }
    }

    private String getCommandIdFromMessage(String message) {
        if (message.startsWith(COMMAND_START)) {
            return message.split(" ")[0].toLowerCase();
        } else if (message.equalsIgnoreCase("/translate")) {
            return AUTO_TRANSLATE.getCommandName();
        } else if (message.equalsIgnoreCase("/set_languages")) {
            return SET_LANGUAGE.getCommandName();
        }
        return null;
    }

    private void executeCommandAndUpdateLog(Update update, String commandId, String firstName, String username, String message) {
        try {
            commandContainer.findCommand(commandId).execute(update);
        } catch (InterruptedException e) {
            log.error("Error executing command: " + commandId, e);
        }
        log.info("This was a response to the user: " +
                firstName + "(" + username + ") to the command: " + message);
    }

    private void handleNonCommandMessage(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        String lastCommand = userService.getLastCommandForUser(userId);
        assert SET_LANGUAGE.getCommandName() != null;
        if (SET_LANGUAGE.getCommandName().equals(lastCommand)) {
            try {
                commandContainer.findCommand(SET_LANGUAGE.getCommandName()).execute(update);
            } catch (InterruptedException e) {
                log.error("Error executing SetLanguageCommand: ", e);
            }
        } else {
            try {
                commandContainer.findCommand(AUTO_TRANSLATE.getCommandName()).execute(update);
            } catch (InterruptedException e) {
                log.error("Error executing TranslateCommand with auto-detection: ", e);
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
