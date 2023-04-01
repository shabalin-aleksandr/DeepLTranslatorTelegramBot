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
import com.telegrambot.deepl.model.LanguagePairSelection;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.TranslateMessageServiceInterface;
import com.telegrambot.deepl.service.UserService;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static com.telegrambot.deepl.command.CommandName.SET_LANGUAGE;

@Slf4j
public class TranslateCommand implements CommandInterface {

    private final TranslateMessageServiceInterface translateMessageServiceInterface;
    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final UserService userService;
    private static final String WRITE_MESSAGE = """
            \s
            \s
            🖋🖋🖋
            Now enter a message for translation, if you already wrote it, then just forward it to me again.
            """;

    public TranslateCommand(TranslateMessageServiceInterface translateMessageServiceInterface,
                            SendMessageServiceInterface sendMessageServiceInterface, UserService userService) {
        this.translateMessageServiceInterface = translateMessageServiceInterface;
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.userService = userService;
    }

    @Override
    public void execute(Update update) throws InterruptedException {
        userService.setLastCommandForUser(update.getMessage().getFrom().getId(), SET_LANGUAGE.getCommandName());
        if (update.hasCallbackQuery()) {
            try {
                handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageToTranslate = update.getMessage().getText();

            if(!userService.isLanguagePairSet(chatId) || "/set_languages".equalsIgnoreCase(messageToTranslate)) {
                Integer messageId = update.getMessage().getMessageId();
                sendSourceLanguageSelectionMessage(chatId, messageId);
            } else {
                LanguagePairSelection languagePair = userService.getUserLanguagePair(chatId);
                log.info("Language pair found for user " + chatId + ": " + languagePair);
                String sourceLanguage = languagePair.sourceLanguage();
                String targetLanguage = languagePair.targetLanguage();

                log.info("The message that the user wanted to translate: " + messageToTranslate);
                log.info("Source Language: " + sourceLanguage + ", Target Language: " + targetLanguage);

                TextResult result = translateMessageServiceInterface.translateMessageWithSourceLanguage(messageToTranslate, sourceLanguage, targetLanguage);
                if (result != null) {
                    String translatedText = result.getText();
                    sendMessageServiceInterface.sendMessage(chatId, translatedText);
                    log.info("Translated message from the bot: " + translatedText);
                } else {
                    sendMessageServiceInterface.sendMessage(chatId, "Sorry, there was an error translating your message.");
                }
            }
        }
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        userService.setLastCommandForUser(callbackQuery.getFrom().getId(), SET_LANGUAGE.getCommandName());

        Long userId = callbackQuery.getFrom().getId();
        String data = callbackQuery.getData();

        if (!userService.hasSelectedSourceLanguage(userId)) {
            if (data.startsWith("source-")) {
                String sourceLanguage = data.substring("source-".length());

                userService.setUserSourceLanguage(userId, sourceLanguage);
                userService.setSelectedSourceLanguage(userId, true);

                sendTargetLanguageSelectionMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), sourceLanguage);


                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
                deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());
                sendMessageServiceInterface.deleteMessage(deleteMessage);

            }
        } else {
            String[] languages = data.split("-");

            if (languages.length == 2) {
                String targetLanguage = languages[1];

                if (targetLanguage.equals("en")) {
                    targetLanguage = "en-US";
                }

                userService.setUserLanguagePair(userId, userService.getUserSourceLanguage(userId), targetLanguage);
                userService.setSelectedSourceLanguage(userId, false);

                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
                editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
                editMessageText.setText("Selected languages: " + getLanguageName(userService.getUserSourceLanguage(userId)) + " ➡ " + getLanguageName(targetLanguage) + WRITE_MESSAGE);

                sendMessageServiceInterface.editMessage(editMessageText);

                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
                answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());

                sendMessageServiceInterface.answerCallbackQuery(answerCallbackQuery);
            }
        }
    }

    protected void sendSourceLanguageSelectionMessage(Long chatId, int messageId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en", "🇩🇪 DE", "de", "source-"));
        keyboard.add(createInlineKeyboardButtonRow("🇨🇿 CZ", "cs", "🇫🇷 FR", "fr", "source-"));
        keyboard.add(createInlineKeyboardButtonRow("🇪🇸 ES", "es", "🇮🇹 IT", "it", "source-"));
        keyboard.add(createInlineKeyboardButtonRow("🇷🇺 RU", "ru", "🇺🇦 UK", "uk", "source-"));

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("🌐 Select source language 🌐");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setReplyToMessageId(messageId);

        try {
            sendMessageServiceInterface.sendMessage(sendMessage);
        } catch (InterruptedException | TelegramApiException e) {
            log.error("Error sending source language selection message: ", e);
        }
    }

    protected void sendTargetLanguageSelectionMessage(Long chatId, int messageId, String sourceLanguage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en", "🇩🇪 DE", "de", sourceLanguage + "-"));
        keyboard.add(createInlineKeyboardButtonRow("🇨🇿 CZ", "cs", "🇫🇷 FR", "fr", sourceLanguage + "-"));
        keyboard.add(createInlineKeyboardButtonRow("🇪🇸 ES", "es", "🇮🇹 IT", "it", sourceLanguage + "-"));
        keyboard.add(createInlineKeyboardButtonRow("🇷🇺 RU", "ru", "🇺🇦 UK", "uk", sourceLanguage + "-"));


        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("🌐 Select target language 🌐");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setReplyToMessageId(messageId);

        try {
            sendMessageServiceInterface.sendMessage(sendMessage);
        } catch (InterruptedException | TelegramApiException e) {
            log.error("Error sending target language selection message: ", e);
        }
    }

    private List<InlineKeyboardButton> createInlineKeyboardButtonRow(String language1,
                                                                     String code1,
                                                                     String language2,
                                                                     String code2,
                                                                     String prefix) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText(language1);
        button1.setCallbackData(prefix + code1);
        row.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText(language2);
        button2.setCallbackData(prefix + code2);
        row.add(button2);

        return row;
    }

    private String getLanguageName(String languageCode) {
        return switch (languageCode) {
            case "en-US", "en" -> "🇺🇸 English (US)";
            case "de" -> "🇩🇪 German";
            case "cs" -> "🇨🇿 Czech";
            case "fr" -> "🇫🇷 French";
            case "es" -> "🇪🇸 Spanish";
            case "it" -> "🇮🇹 Italian";
            case "ru" -> "🇷🇺 Russian";
            case "uk" -> "🇺🇦 Ukrainian";
            default -> "⭕️ Unknown";
        };
    }
}
