package com.telegrambot.deepl.command;

public enum CommandName {
    START("/start"),
    DELETE("/deletemydata"),
    HELP("/help"),
    WRONG("nocommand"),
    TRANSLATE("/tr"),
    LANGUAGES("/lang")
    ;

    private final String commandName;

    CommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
