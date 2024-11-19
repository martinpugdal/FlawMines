package dk.martinersej.plugin.utils.command;

public enum Result {
    NO_PERMISSION, NO_SUB_COMMAND_FOUND, SUCCESS, WRONG_USAGE, CONSOLE_ONLY, PLAYER_ONLY, ERROR;

    public static CommandResult getCommandResult(SubCommand subCommand, Result result, String message) {
        return new CommandResult(subCommand, result, message);
    }

    public static CommandResult getCommandResult(SubCommand subCommand, Result result) {
        return getCommandResult(subCommand, result, null);
    }

    public static CommandResult noPermission(SubCommand subCommand) {
        return getCommandResult(subCommand, NO_PERMISSION);
    }

    public static CommandResult noSubCommandFound(SubCommand subCommand) {
        return getCommandResult(subCommand, NO_SUB_COMMAND_FOUND);
    }

    public static CommandResult success(SubCommand subCommand) {
        return getCommandResult(subCommand, SUCCESS);
    }

    public static CommandResult wrongUsage(SubCommand subCommand) {
        return getCommandResult(subCommand, WRONG_USAGE);
    }

    public static CommandResult consoleOnly(SubCommand subCommand) {
        return getCommandResult(subCommand, CONSOLE_ONLY);
    }

    public static CommandResult playerOnly(SubCommand subCommand) {
        return getCommandResult(subCommand, PLAYER_ONLY);
    }

    public static CommandResult error(SubCommand subCommand) {
        return getCommandResult(subCommand, ERROR);
    }

    public static CommandResult error(SubCommand subCommand, String message) {
        return getCommandResult(subCommand, ERROR, message);
    }

    public boolean isSuccessful() {
        return this == SUCCESS;
    }
}
