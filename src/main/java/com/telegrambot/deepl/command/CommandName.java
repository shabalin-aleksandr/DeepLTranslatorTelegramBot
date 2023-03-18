package com.telegrambot.deepl.command;

public enum CommandName {
    START("/start"),
    DELETE("/deletemydata"),
    HELP("/help"),
    WRONG("nocommand")
    ;

    private final String commandName;

    CommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
