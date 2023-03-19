package com.telegrambot.deepl.command;

import com.deepl.api.TextResult;
import com.telegrambot.deepl.bot.DeepLTelegramBot;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.TranslateMessageServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TranslateCommand implements CommandInterface {

    private final TranslateMessageServiceInterface translateMessageServiceInterface;
    private final SendMessageServiceInterface sendMessageServiceInterface;
    private final Map<Long, BlockingQueue<String>> userInputQueues = new ConcurrentHashMap<>();

    private DeepLTelegramBot deepLBot;


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
