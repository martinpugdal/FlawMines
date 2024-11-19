package dk.martinersej.plugin.command;

import dk.martinersej.plugin.command.subcommands.*;
import dk.martinersej.plugin.utils.command.Command;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import org.bukkit.command.CommandSender;

public class BaseCommand extends Command {

    public BaseCommand() {
        super("flawmines", new String[]{"fm"}, "Main command for FlawMines", "/flawmines <subcommand>");

        addSubCommand(new CreateMineCommand());
        addSubCommand(new RemoveMineCommand());
        addSubCommand(new ResetMineCommand());
        addSubCommand(new AddBlockCommand());
        addSubCommand(new RemoveBlockCommand());
        addSubCommand(new ListBlockCommand());
    }

    @Override
    public boolean run(CommandSender commandSender, String label, String... args) {
        CommandResult result = runSubCommand(commandSender, args);
        if (result.getMessage() != null) {
            commandSender.sendMessage(result.getMessage());
            return true;
        }

        if (result.getResult() == Result.WRONG_USAGE) {
            commandSender.sendMessage(result.getSubCommand().getUsage(label));
            return true;
        }

        return result.getResult().isSuccessful();
    }
}
