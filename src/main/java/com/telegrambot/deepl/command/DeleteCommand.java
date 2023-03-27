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
import com.telegrambot.deepl.service.UserService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class DeleteCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final UserService userService;

    public final static String DELETE_MESSAGE = """
            ✅Your data were successfully deleted.✅
            
            If you want to go back, just type /start again.
            
            To clear your chat history with this bot, please follow these steps:
            1. Tap on the bot's name at the top of the chat.
            2. Tap on 'Clear History' (on mobile) or 'Delete Chat' (on desktop).
            3. Confirm the action.
            """;

    public DeleteCommand(SendMessageServiceInterface sendMessageServiceInterface, UserService userService) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.userService = userService;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();

        userService.removeUserLanguages(chatId);
        userService.deleteUser(update.getMessage());
        sendMessageServiceInterface.sendMessage(chatId, DELETE_MESSAGE);
    }
}
