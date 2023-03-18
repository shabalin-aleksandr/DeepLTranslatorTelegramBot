package com.telegrambot.deepl.bot;

import com.telegrambot.deepl.command.CommandContainer;
import com.telegrambot.deepl.config.BotConfig;
import com.telegrambot.deepl.service.SendMessageService;
import com.telegrambot.deepl.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.telegrambot.deepl.command.CommandName.WRONG;

@Component
public class DeepLTelegramBot extends TelegramLongPollingBot {

    public static String COMMAND_START = "/";
    final BotConfig config;
    private final CommandContainer commandContainer;


    @Autowired
    public DeepLTelegramBot(UserService userService, BotConfig config, List<String> admins) {
        this.config = config;
        this.commandContainer = new CommandContainer(new SendMessageService(this), userService, admins);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText().trim();
            String username = update.getMessage().getFrom().getUserName();

            if (message.startsWith(COMMAND_START)) {
                String commandId = message.split(" ")[0].toLowerCase();
                commandContainer.findCommand(commandId, username).execute(update);
            } else {
                commandContainer.findCommand(WRONG.getCommandName(), username).execute(update);
            }
        }
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }
}
