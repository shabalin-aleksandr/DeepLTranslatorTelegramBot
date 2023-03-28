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

import com.telegrambot.deepl.config.BotConfig;
import com.telegrambot.deepl.repository.UserRepository;
import com.telegrambot.deepl.repository.UserRepositoryInterface;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@Slf4j
public class SendMessageToAllCommand implements CommandInterface {

    private final UserRepositoryInterface userRepository;
    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final BotConfig config;
    private final UnknownCommand unknownCommand;

    public SendMessageToAllCommand(UserRepositoryInterface userRepository,
                                   SendMessageServiceInterface sendMessageServiceInterface, BotConfig config) {
        this.userRepository = userRepository;
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.config = config;
        this.unknownCommand = new UnknownCommand(sendMessageServiceInterface);
    }

    @Override
    public void execute(Update update) throws InterruptedException {
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getUserName();

        if (Objects.equals(config.getAdminId(), chatId)) {
            String messageText = update.getMessage().getText();
            String textToSend = messageText.substring(messageText.indexOf(" "));
            var users = userRepository.findAll();

            for (UserRepository user : users) {
                sendMessageServiceInterface.sendMessage(user.getChatId(), textToSend);
            }
            log.info("Admin: " + username + " with id: " + chatId + " using an Admin command");
        } else {
            unknownCommand.execute(update);
            log.info("User: " +username + " with id: " + chatId + " was trying to use Admin command");
        }
    }
}

