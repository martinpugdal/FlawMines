package dk.martinersej.plugin.utils.command;

public class CommandResult {

    private final SubCommand subCommand;
    private final Result result;
    private final String message;

    public CommandResult(SubCommand subCommand, Result result) {
        this(subCommand, result, null);
    }

    public CommandResult(SubCommand subCommand, Result result, String message) {
        this.subCommand = subCommand;
        this.result = result;
        this.message = message;
    }

    public SubCommand getSubCommand() {
        return subCommand;
    }

    public Result getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}