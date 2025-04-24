package ua.mykola.botservice.model;

import java.util.Arrays;

public enum BotCommand {
    START("/start"),
    BOOK("/book"),
    DETAILS_MY_BOOKINGS("/details");

    private final String command;

    BotCommand(String command) {
        this.command = command;
    }

    public static BotCommand fromString(String text) {
        return Arrays.stream(values())
                .filter(cmd -> cmd.command.equalsIgnoreCase(text))
                .findFirst()
                .orElse(null);
    }

    public String getCommand() {
        return command;
    }
}
