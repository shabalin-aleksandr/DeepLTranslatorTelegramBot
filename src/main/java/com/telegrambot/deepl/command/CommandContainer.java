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

import com.google.common.collect.ImmutableMap;
import com.telegrambot.deepl.bot.DeepLTelegramBot;
import com.telegrambot.deepl.config.BotConfig;
import com.telegrambot.deepl.repository.UserRepositoryInterface;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.TranslateMessageServiceInterface;
import com.telegrambot.deepl.service.UserService;
import static com.telegrambot.deepl.command.CommandName.*;

/**
 * {@link CommandName}
 */
public class CommandContainer {

    private final CommandInterface unknownCommand;
    private final ImmutableMap<String, CommandInterface> commandMap;

    public CommandContainer(SendMessageServiceInterface sendMessageServiceInterface,
                            TranslateMessageServiceInterface translateMessageServiceInterface,
                            UserService userService, UserRepositoryInterface userRepository,
                            BotConfig config,
                            DeepLTelegramBot deeplBot) {

        commandMap = ImmutableMap.<String, CommandInterface>builder()
                .put(START.getCommandName(), new StartCommand(sendMessageServiceInterface, userService, deeplBot))
                .put(DELETE.getCommandName(), new DeleteCommand(sendMessageServiceInterface, userService))
                .put(HELP.getCommandName(), new HelpCommand(sendMessageServiceInterface))
                .put(TRANSLATE.getCommandName(), new TranslateCommand(translateMessageServiceInterface,
                        sendMessageServiceInterface, userService))
                .put(LANGUAGES.getCommandName(), new LanguagesCommand(sendMessageServiceInterface))
                .put(ADMIN_SEND_COMMAND.getCommandName(), new SendMessageToAllCommand(userRepository,
                        sendMessageServiceInterface, config))
                .build();

        unknownCommand = new UnknownCommand(sendMessageServiceInterface);
    }

    public CommandInterface findCommand(String commandId) {
        return commandMap.getOrDefault(commandId, unknownCommand);
    }
}
