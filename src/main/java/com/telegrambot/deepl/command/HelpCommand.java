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
            
            /deletemydata - This command will delete all data about you, as well as terminate the bot
            
            /tr - The command will translate the message you send to the bot
            
            /lang - To see a list of available languages that the bot understands
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
