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
import com.telegrambot.deepl.model.LanguageSelection;
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

import static com.telegrambot.deepl.command.CommandName.AUTO_TRANSLATE;

@Slf4j
public class AutoTranslateCommand implements CommandInterface {

    private final TranslateMessageServiceInterface translateMessageServiceInterface;
    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final UserService userService;

    public final static String TRANSLATE_MESSAGE = """
            If your translation isn't correct, you can always select specific languages with the command 👉 /setlanguages\s
            
            👇 Here's your translated message 👇
            """;

    private static final String SELECT_LANGUAGE_MESSAGE = "🌐 Please choose the language you want me to translate your message into 🌐";
    private static final String WRITE_MESSAGE = """
            \s
            🖋🖋🖋
            Now enter a message for translation, if you already wrote it, then just forward it to me again.
            """;

    public AutoTranslateCommand(TranslateMessageServiceInterface translateMessageServiceInterface, SendMessageServiceInterface sendMessageServiceInterface, UserService userService) {
        this.translateMessageServiceInterface = translateMessageServiceInterface;
        this.sendMessageServiceInterface = sendMessageServiceInterface;
        this.userService = userService;
    }

    @Override
    public void execute(Update update) throws InterruptedException {
        userService.setLastCommandForUser(Math.toIntExact(update.getMessage().getFrom().getId()), AUTO_TRANSLATE.getCommandName());

        if (update.hasCallbackQuery()) {
            try {
                handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageToTranslate = update.getMessage().getText();
            String username = update.getMessage().getFrom().getUserName();

            if (!userService.isLanguageSet(chatId) || "/translate".equalsIgnoreCase(messageToTranslate)) {
                Integer messageId = update.getMessage().getMessageId();
                sendLanguageSelectionMessage(chatId, messageId);
            } else {
                LanguageSelection selectedLanguage = userService.getUserLanguage(Math.toIntExact(chatId));
                log.info("The user: " + username + " has selected a language to translate the message: " + selectedLanguage);
                String targetLanguage = convertEnToEnUs(selectedLanguage.targetLanguage());

                log.info("The message that the user wanted to translate: \"" + messageToTranslate + "\" into <" + targetLanguage + "> language");

                TextResult result = translateMessageServiceInterface.translateAutoDetectedLanguage(messageToTranslate, targetLanguage);
                if (result != null) {
                    String translatedText = result.getText();
                    sendMessageServiceInterface.sendMessage(chatId, TRANSLATE_MESSAGE);
                    sendMessageServiceInterface.sendMessage(chatId, translatedText);
                    log.info("Translated message from the bot: " + translatedText);
                } else {
                    sendMessageServiceInterface.sendMessage(chatId, "\uD83E\uDD2B An unexpected error occurred during translation. I may not be able to recognise the language. " +
                            "Try to set up a pair of languages with /setlanguages or write to the administrator if I still don't know your language.");
                }
            }
        }
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        userService.setLastCommandForUser(Math.toIntExact(callbackQuery.getFrom().getId()), AUTO_TRANSLATE.getCommandName());
        String languageCode = callbackQuery.getData();
        String targetLanguage = convertEnToEnUs(languageCode);

        userService.setUserLanguage(Math.toIntExact(callbackQuery.getFrom().getId()), targetLanguage);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageText.setText("Selected language: " + getLanguageName(targetLanguage) + WRITE_MESSAGE);

        sendMessageServiceInterface.editMessage(editMessageText);

        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());

        sendMessageServiceInterface.answerCallbackQuery(answerCallbackQuery);
    }

    protected void sendLanguageSelectionMessage(Long chatId, int messageId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createInlineKeyboardButtonRow("🇺🇸 EN", "en-US"));
        keyboard.add(createInlineKeyboardButtonRow("🇩🇪 DE", "de"));
        keyboard.add(createInlineKeyboardButtonRow("🇨🇿 CZ", "cs"));
        keyboard.add(createInlineKeyboardButtonRow("🇫🇷 FR", "fr"));
        keyboard.add(createInlineKeyboardButtonRow("🇪🇸 ES", "es"));
        keyboard.add(createInlineKeyboardButtonRow("🇮🇹 IT", "it"));
        keyboard.add(createInlineKeyboardButtonRow("🇷🇺 RU", "ru"));
        keyboard.add(createInlineKeyboardButtonRow("🇺🇦 UK", "uk"));

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(SELECT_LANGUAGE_MESSAGE);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setReplyToMessageId(messageId);

        try {
            sendMessageServiceInterface.sendMessage(sendMessage);
        } catch (InterruptedException | TelegramApiException e) {
            log.error("Error sending language selection message: ", e);
        }
    }

    private List<InlineKeyboardButton> createInlineKeyboardButtonRow(String targetLanguage1, String targetCode) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText(String.format("%s", targetLanguage1));
        button1.setCallbackData(String.format("%s", targetCode));
        row.add(button1);

        return row;
    }

    private String convertEnToEnUs(String languageCode) {
        if (languageCode == null) {
            return null;
        }

        if (languageCode.contains("-")) {
            String[] parts = languageCode.split("-");
            if ("en".equalsIgnoreCase(parts[1])) {
                return parts[0] + "-US";
            }
        }

        return languageCode;
    }

    private String getLanguageName(String languageCode) {
        return switch (languageCode) {
            case "en-US" -> "🇺🇸 English (US)";
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
