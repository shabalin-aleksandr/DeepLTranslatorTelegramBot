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

package com.telegrambot.deepl.service;

import com.telegrambot.deepl.bot.DeepLTelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendMessageServiceTest {

    SendMessageServiceInterface sendMessageServiceInterface;
    DeepLTelegramBot deeplBot;
    UserService userService;

    @BeforeEach
    public void init(){
        deeplBot = Mockito.mock(DeepLTelegramBot.class);
        sendMessageServiceInterface = new SendMessageService(deeplBot, userService);
    }

    @Test
    public void shouldCorrectlySendMessage() throws TelegramApiException {
        Long chatId = 123456789L;
        String message = "some_text";

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(message);
        sendMessage.enableHtml(true);

        sendMessageServiceInterface.sendMessage(chatId, message);

        Mockito.verify(deeplBot).execute(sendMessage);
    }
}
