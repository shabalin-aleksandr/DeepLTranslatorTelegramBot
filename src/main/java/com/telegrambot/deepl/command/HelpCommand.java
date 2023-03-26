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

public class HelpCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;

    public final static String HELP_MESSAGE = """
            ℹ️HELP MENUℹ️
            
            Here you can see commands what I can understand:
            
            /start - Display greeting message
            
            /help - Display info about acceptable commands
            
            /tr - The command will translate the message you send to the bot
            
            /change - This command will change your language preferences
            
            /languages - To see a list of available languages that the bot understands
            
            /deletemydata - This command will delete all data about you, as well as terminate the bot
            
            """;

    public HelpCommand(SendMessageServiceInterface sendMessageServiceInterface) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();

        sendMessageServiceInterface.sendMessage(chatId, HELP_MESSAGE);
    }
}
