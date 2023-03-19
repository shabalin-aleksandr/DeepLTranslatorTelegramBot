package com.telegrambot.deepl.command;

import com.deepl.api.TextResult;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.TranslateMessageService;
import com.telegrambot.deepl.service.TranslateMessageServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
public class TranslateCommand implements CommandInterface {

    private final TranslateMessageServiceInterface translateMessageServiceInterface;
    private final SendMessageServiceInterface sendMessageServiceInterface;


    public final static String TRANSLATE_MESSAGE = """
            Please enter the text you wish to translate:\s
            """;

    public TranslateCommand(TranslateMessageServiceInterface translateMessageServiceInterface,
                            SendMessageServiceInterface sendMessageServiceInterface) {
        this.translateMessageServiceInterface = translateMessageServiceInterface;
        this.sendMessageServiceInterface = sendMessageServiceInterface;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageToTranslate = update.getMessage().getText();

        log.info("The message that the user wanted to translate: " + messageToTranslate);


        sendMessageServiceInterface.sendMessage(chatId, TRANSLATE_MESSAGE);

        TextResult result = translateMessageServiceInterface.translateMessage(messageToTranslate);

        if (result != null) {
            String translatedText = result.getText();
            sendMessageServiceInterface.sendMessage(chatId, translatedText);
            log.info("Translated message from the bot: " + translatedText);
        } else {
            sendMessageServiceInterface.sendMessage(chatId, "Sorry, there was an error translating your message.");
        }

    }
}
