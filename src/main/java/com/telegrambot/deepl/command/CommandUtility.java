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
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandUtility {

    public static void setTranslateButton(SendMessageServiceInterface sendMessageServiceInterface,
                                          String buttonText,
                                          String callbackData,
                                          Long chatId,
                                          String messageText) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton translateButton = new InlineKeyboardButton(buttonText);
        translateButton.setCallbackData(callbackData);

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(translateButton);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(keyboardButtonsRow);

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage startMessage = new SendMessage();
        startMessage.setChatId(String.valueOf(chatId));
        startMessage.setText(messageText);
        startMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            sendMessageServiceInterface.sendMessage(startMessage);
        } catch (InterruptedException | TelegramApiException e) {
            log.error("Error sending language selection message: ", e);
        }
    }

    public static void handleTranslateCallbackQuery(SendMessageServiceInterface sendMessageServiceInterface,
                                                    String callbackData,
                                                    CallbackQuery callbackQuery,
                                                    String translatedText) throws TelegramApiException {
        if (callbackQuery.getData().equals(callbackData)) {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
            editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
            editMessageText.setText(translatedText);

            sendMessageServiceInterface.editMessage(editMessageText);

            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());

            sendMessageServiceInterface.answerCallbackQuery(answerCallbackQuery);
        }
    }
}
