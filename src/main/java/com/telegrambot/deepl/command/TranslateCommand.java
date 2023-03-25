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

import com.deepl.api.TextResult;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.TranslateMessageServiceInterface;
import org.telegram.telegrambots.meta.api.objects.Update;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TranslateCommand implements CommandInterface {

    private final TranslateMessageServiceInterface translateMessageServiceInterface;
    private final SendMessageServiceInterface sendMessageServiceInterface;

    public final static String TRANSLATE_MESSAGE = """
            Here's your translated message:\s
            """;

    public TranslateCommand(TranslateMessageServiceInterface translateMessageServiceInterface,
                            SendMessageServiceInterface sendMessageServiceInterface) {
        this.translateMessageServiceInterface = translateMessageServiceInterface;
        this.sendMessageServiceInterface = sendMessageServiceInterface;
    }

    @Override
    public void execute(Update update) throws InterruptedException {
        Long chatId = update.getMessage().getChatId();
        String messageToTranslate = update.getMessage().getText();

        log.info("The message that the user wanted to translate: " + messageToTranslate);

        sendMessageServiceInterface.sendMessage(chatId, TRANSLATE_MESSAGE);

        TextResult result = translateMessageServiceInterface.translateMessage(messageToTranslate);
        if (result != null) {
            String translatedText = result.getText().substring(3);
            sendMessageServiceInterface.sendMessage(chatId, translatedText);
            log.info("Translated message from the bot: " + translatedText);
        } else {
            sendMessageServiceInterface.sendMessage(chatId, "Sorry, there was an error translating your message.");
        }
    }
}
