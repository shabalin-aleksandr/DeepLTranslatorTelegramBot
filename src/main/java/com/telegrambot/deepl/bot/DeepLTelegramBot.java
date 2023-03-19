package com.telegrambot.deepl.bot;

import com.telegrambot.deepl.command.CommandContainer;
import com.telegrambot.deepl.config.BotConfig;
import com.telegrambot.deepl.service.SendMessageService;
import com.telegrambot.deepl.service.TranslateMessageService;
import com.telegrambot.deepl.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static com.telegrambot.deepl.command.CommandName.WRONG;

@Slf4j
@Component
public class DeepLTelegramBot extends TelegramLongPollingBot {

    public static String COMMAND_START = "/";
    final BotConfig config;
    private final CommandContainer commandContainer;


    @Autowired
    public DeepLTelegramBot(UserService userService, BotConfig config, List<String> admins) {
        this.config = config;
        this.commandContainer = new CommandContainer(new SendMessageService(this),
                new TranslateMessageService(this),
                userService,
                admins);

        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/start", "Get a welcome message"));
        botCommands.add(new BotCommand("/help", "Info about commands"));
        botCommands.add(new BotCommand("/deletemydata", "Delete your account"));
        botCommands.add(new BotCommand("/tr", "Translate message"));
        botCommands.add(new BotCommand("/lang", "List of languages"));

        try {
            this.execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText().trim();
            String username = update.getMessage().getFrom().getUserName();
            String firstName = update.getMessage().getChat().getFirstName();

            if (message.startsWith(COMMAND_START)) {
                String commandId = message.split(" ")[0].toLowerCase();
                try {
                    commandContainer.findCommand(commandId, username).execute(update);
                } catch (InterruptedException e) {
                    log.error("Error occurred: " + e.getMessage());
                }
                log.info("This was a response to the user: " +
                        firstName + "(" + username + ") to the command: " + message);
            } else {
                try {
                    commandContainer.findCommand(WRONG.getCommandName(), username).execute(update);
                } catch (InterruptedException e) {
                    log.error("Error occurred: " + e.getMessage());
                }
                log.info("Got a wrong message/command from user: " +
                        firstName + "(" + username + "). Got this message: " + message);
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
