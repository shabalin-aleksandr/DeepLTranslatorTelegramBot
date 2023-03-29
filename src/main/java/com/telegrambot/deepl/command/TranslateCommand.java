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

    public final static String TRANSLATE_MESSAGE = """
            Here's your translated message:
            """;
    private static final String SElECT_LANGUAGE_PAIR_MESSAGE = "Please select the source and target language:";

    public TranslateCommand(TranslateMessageServiceInterface translateMessageServiceInterface,
                            SendMessageServiceInterface sendMessageServiceInterface, UserService userService) {
        this.translateMessageServiceInterface = translateMessageServiceInterface;
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.userService = userService;
    }

    @Override
    public void execute(Update update) throws InterruptedException {
        userService.setLastCommandForUser(Math.toIntExact(update.getMessage().getFrom().getId()), SET_LANGUAGE.getCommandName());
        if (update.hasCallbackQuery()) {
            try {
                handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageToTranslate = update.getMessage().getText();

            if(!userService.isLanguagePairSet(chatId) || "/setlanguages".equalsIgnoreCase(messageToTranslate)) {
                Integer messageId = update.getMessage().getMessageId();
                sendMessageServiceInterface.sendMessage(chatId, "Please select a pair of languages from the menu:");
                sendLanguagePairSelectionMessage(chatId, messageId);
            } else {
                LanguagePairSelection languagePair = userService.getUserLanguagePair(Math.toIntExact(chatId));
                log.info("Language pair found for user " + chatId + ": " + languagePair);
                String sourceLanguage = languagePair.sourceLanguage();
                String targetLanguage = languagePair.targetLanguage();

                log.info("The message that the user wanted to translate: " + messageToTranslate);

                sendMessageServiceInterface.sendMessage(chatId, TRANSLATE_MESSAGE);

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
        userService.setLastCommandForUser(Math.toIntExact(callbackQuery.getFrom().getId()), SET_LANGUAGE.getCommandName());

        String[] languageCodes = callbackQuery.getData().split("-");
        String sourceLanguage = languageCodes[0];
        String targetLanguage = convertEnToEnUs(languageCodes[1]);

        userService.setUserLanguagePair(Math.toIntExact(callbackQuery.getFrom().getId()), sourceLanguage, targetLanguage);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageText.setText("Selected languages: " + sourceLanguage + " ➡ " + targetLanguage);

        sendMessageServiceInterface.editMessage(editMessageText);

        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());

        sendMessageServiceInterface.answerCallbackQuery(answerCallbackQuery);
    }

    protected void sendLanguagePairSelectionMessage(Long chatId, int messageId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en", "🇨🇿 CZ", "cs", "🇨🇿 CZ", "cs", "🇺🇸 EN", "en-US"));
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en", "🇩🇪 DE", "de", "🇩🇪 DE", "de", "🇺🇸 EN", "en-US"));
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en", "🇷🇺 RU", "ru", "🇷🇺 RU", "ru", "🇺🇸 EN", "en-US"));
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en", "🇺🇦 UK", "uk", "🇺🇦 UK", "uk", "🇺🇸 EN", "en-US"));
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en", "🇫🇷 FR", "fr", "🇫🇷 FR", "fr", "🇺🇸 EN", "en-US"));
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en", "🇪🇸 ES", "es", "🇪🇸 ES", "es", "🇺🇸 EN", "en-US"));
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en", "🇮🇹 IT", "it", "🇮🇹 IT", "it", "🇺🇸 EN", "en-US"));
        keyboard.add(createInlineKeyboardButtonRow("🇩🇪 DE", "de", "🇨🇿 CZ", "cs", "🇨🇿 CZ", "cs", "🇩🇪 DE", "de"));
        keyboard.add(createInlineKeyboardButtonRow("🇩🇪 DE", "de", "🇷🇺 RU", "ru", "🇷🇺 RU", "ru", "🇩🇪 DE", "de"));
        keyboard.add(createInlineKeyboardButtonRow("🇩🇪 DE", "de", "🇫🇷 FR", "fr", "🇫🇷 FR", "fr", "🇩🇪 DE", "de"));
        keyboard.add(createInlineKeyboardButtonRow("🇩🇪 DE", "de", "🇮🇹 IT", "it", "🇮🇹 IT", "it", "🇩🇪 DE", "de"));
        keyboard.add(createInlineKeyboardButtonRow("🇩🇪 DE", "de", "🇺🇦 UK", "uk", "🇺🇦 UK", "uk", "🇩🇪 DE", "de"));

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(SElECT_LANGUAGE_PAIR_MESSAGE);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setReplyToMessageId(messageId);

        try {
            sendMessageServiceInterface.sendMessage(sendMessage);
        } catch (InterruptedException | TelegramApiException e) {
            log.error("Error sending language selection message: ", e);
        }
    }

    private String convertEnToEnUs(String lang) {
        if (lang.equals("en")) {
            return "en-US";
        }
        return lang;
    }

    private List<InlineKeyboardButton> createInlineKeyboardButtonRow(String sourceLanguage1, String sourceCode1,
                                                                     String targetLanguage1, String targetCode1,
                                                                     String sourceLanguage2, String sourceCode2,
                                                                     String targetLanguage2, String targetCode2) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText(String.format("%s - %s", sourceLanguage1, targetLanguage1));
        button1.setCallbackData(String.format("%s-%s", sourceCode1, targetCode1));
        row.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText(String.format("%s - %s", sourceLanguage2, targetLanguage2));
        button2.setCallbackData(String.format("%s-%s", sourceCode2, targetCode2));
        row.add(button2);

        return row;
    }
}
