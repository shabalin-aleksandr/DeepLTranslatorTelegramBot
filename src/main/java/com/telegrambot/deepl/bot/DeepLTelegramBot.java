package com.telegrambot.deepl.bot;

import com.telegrambot.deepl.command.CommandContainer;
import com.telegrambot.deepl.config.BotConfig;
import com.telegrambot.deepl.service.SendMessageService;
import com.telegrambot.deepl.service.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class DeepLTelegramBot extends TelegramLongPollingBot {

    public static String COMMAND_START = "/";
    final BotConfig config;

    private final CommandContainer commandContainer;

    public DeepLTelegramBot(UserService userService, BotConfig config, CommandContainer commandContainer) {
        this.config = config;
        this.commandContainer = new CommandContainer(new SendMessageService(this), userService);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText().trim();
            String username = update.getMessage().getFrom().getUserName();
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
