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
import com.telegrambot.deepl.config.BotConfig;
import com.telegrambot.deepl.repository.UserRepository;
import com.telegrambot.deepl.repository.UserRepositoryInterface;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.TranslateMessageServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

@Slf4j
public class SendMessageToAllCommand implements CommandInterface {

    private final UserRepositoryInterface userRepository;
    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final BotConfig config;
    private final UnknownCommand unknownCommand;
    private final TranslateMessageServiceInterface translateMessageServiceInterface;

    public SendMessageToAllCommand(UserRepositoryInterface userRepository,
                                   SendMessageServiceInterface sendMessageServiceInterface, BotConfig config, TranslateMessageServiceInterface translateMessageServiceInterface) {
        this.userRepository = userRepository;
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.config = config;
        this.unknownCommand = new UnknownCommand(sendMessageServiceInterface);
        this.translateMessageServiceInterface = translateMessageServiceInterface;
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
                setTranslateButtonAdminMessage(user.getChatId(), textToSend);
            }
            log.info("Admin: " + username + " with id: " + chatId + " using an Admin command: /send");
        } else {
            unknownCommand.execute(update);
            log.info("User: " +username + " with id: " + chatId + " was trying to use Admin command");
        }
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        String callbackData = callbackQuery.getData();

        if (callbackData.equals("translate_russian_admin")) {
            String originalMessage = callbackQuery.getMessage().getText();
            TextResult translatedResult = translateToRussian(originalMessage);

            if (translatedResult != null) {
                String translatedMessage = translatedResult.getText();
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
                editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
                editMessageText.setText(translatedMessage);

                sendMessageServiceInterface.editMessage(editMessageText);

                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
                answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());

                sendMessageServiceInterface.answerCallbackQuery(answerCallbackQuery);
            } else {
                log.error("Error translating admin message");
            }
        }
    }


    private void setTranslateButtonAdminMessage(Long chatId, String textToSendEn) {
        CommandUtility.setTranslateButton(sendMessageServiceInterface,
                "Перевести на русский язык 🇷🇺",
                "translate_russian_admin",
                chatId,
                textToSendEn);
    }

    private TextResult translateToRussian(String message) {
        return translateMessageServiceInterface.translateAutoDetectedLanguage(message, "ru");
    }
}

