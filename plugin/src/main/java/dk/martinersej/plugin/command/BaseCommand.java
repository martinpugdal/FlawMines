package dk.martinersej.plugin.command;

import dk.martinersej.plugin.command.subcommands.block.AddBlockCommand;
import dk.martinersej.plugin.command.subcommands.block.ListBlockCommand;
import dk.martinersej.plugin.command.subcommands.block.RemoveBlockCommand;
import dk.martinersej.plugin.command.subcommands.environment.AddEnvironmentCommand;
import dk.martinersej.plugin.command.subcommands.environment.ListEnvironmentCommand;
import dk.martinersej.plugin.command.subcommands.environment.RemoveEnvironmentCommand;
import dk.martinersej.plugin.command.subcommands.mine.CreateMineCommand;
import dk.martinersej.plugin.command.subcommands.mine.ListMinesCommand;
import dk.martinersej.plugin.command.subcommands.mine.RemoveMineCommand;
import dk.martinersej.plugin.command.subcommands.mine.ResetMineCommand;
import dk.martinersej.plugin.command.subcommands.utility.FillmodeMineCommand;
import dk.martinersej.plugin.command.subcommands.utility.SetTeleportMineCommand;
import dk.martinersej.plugin.utils.command.Command;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import org.bukkit.command.CommandSender;

public class BaseCommand extends Command {

    public BaseCommand() {
        super("flawmines", new String[]{"fm"}, "Main command for FlawMines", "/flawmines <subcommand>");

        addSubCommand(new SetTeleportMineCommand());
        addSubCommand(new FillmodeMineCommand());

        addSubCommand(new CreateMineCommand());
        addSubCommand(new RemoveMineCommand());
        addSubCommand(new ResetMineCommand());
        addSubCommand(new ListMinesCommand());

        addSubCommand(new AddBlockCommand());
        addSubCommand(new RemoveBlockCommand());
        addSubCommand(new ListBlockCommand());

        addSubCommand(new AddEnvironmentCommand());
        addSubCommand(new RemoveEnvironmentCommand());
        addSubCommand(new ListEnvironmentCommand());
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
