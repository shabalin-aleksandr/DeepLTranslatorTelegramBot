package com.telegrambot.deepl.command;

import com.google.common.collect.ImmutableMap;
import com.telegrambot.deepl.command.annotation.AdminCommand;
import com.telegrambot.deepl.service.SendMessageService;
import com.telegrambot.deepl.service.SendMessageServiceInterface;
import com.telegrambot.deepl.service.TranslateMessageServiceInterface;
import com.telegrambot.deepl.service.UserService;
import java.util.List;
import static com.telegrambot.deepl.command.CommandName.*;
import static java.util.Objects.nonNull;

public class CommandContainer {
    private final CommandInterface unknownCommand;
    private final ImmutableMap<String, CommandInterface> commandMap;
    private final List<String> admins;

    public CommandContainer(SendMessageServiceInterface sendMessageServiceInterface,
                            TranslateMessageServiceInterface translateMessageServiceInterface,
                            UserService userService, List<String> admins) {
        this.admins = admins;

        commandMap = ImmutableMap.<String, CommandInterface>builder()
                .put(START.getCommandName(), new StartCommand(sendMessageServiceInterface, userService))
                .put(DELETE.getCommandName(), new DeleteCommand(sendMessageServiceInterface, userService))
                .put(HELP.getCommandName(), new HelpCommand(sendMessageServiceInterface))
                .put(WRONG.getCommandName(), new WrongCommand(sendMessageServiceInterface))
                .put(TRANSLATE.getCommandName(), new TranslateCommand(translateMessageServiceInterface,
                        sendMessageServiceInterface))
                .put(LANGUAGES.getCommandName(), new LanguagesCommand(sendMessageServiceInterface))
                .build();

        unknownCommand = new UnknownCommand((SendMessageService) sendMessageServiceInterface);
    }

    public CommandInterface findCommand(String commandId, String username) {
        CommandInterface orDefault = commandMap.getOrDefault(commandId, unknownCommand);
        if (isAdminCommand(orDefault)) {
            if (admins.contains(username)) {
                return orDefault;
            } else {
                return unknownCommand;
            }
        }
        return orDefault;
    }

    private boolean isAdminCommand(CommandInterface commandName) {
        return nonNull(commandName.getClass().getAnnotation(AdminCommand.class));
    }

}
