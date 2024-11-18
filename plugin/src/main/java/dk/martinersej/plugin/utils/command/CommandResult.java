package dk.martinersej.plugin.utils.command;

public class CommandResult {

    private final SubCommand subCommand;
    private final Result result;

    public CommandResult(SubCommand subCommand, Result result) {
        this.subCommand = subCommand;
        this.result = result;
    }

    public SubCommand getSubCommand() {
        return subCommand;
    }

    public Result getResult() {
        return result;
    }
}