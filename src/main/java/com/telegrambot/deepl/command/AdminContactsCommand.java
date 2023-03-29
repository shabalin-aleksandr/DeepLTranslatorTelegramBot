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
import org.telegram.telegrambots.meta.api.objects.Update;

public class AdminContactsCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;

    public final static String ADMIN_CONTACTS_MESSAGE = """
            
            ✨ In case of any questions, you can contact the Admin of this bot ✨
                        
            ✈️ Telegram: @Doberman786
            📩 Gmail: dev.aleksandr2000@gmail.com
            📸 Instagram: https://www.instagram.com/_dbrmn_/
                        
            📣 Also, you can send your feedback regarding the use of the bot, this will help make it better.
            """;

    public AdminContactsCommand(SendMessageServiceInterface sendMessageServiceInterface) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();

        sendMessageServiceInterface.sendMessage(chatId, ADMIN_CONTACTS_MESSAGE);
    }
}
