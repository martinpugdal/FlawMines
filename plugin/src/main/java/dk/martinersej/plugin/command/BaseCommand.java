package dk.martinersej.plugin.command;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.utils.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class BaseCommand extends Command {

    public BaseCommand(String name, String... aliases) {
        super(name, aliases);

        inject(FlawMines.get());
    }

    @Override
    public boolean run(CommandSender commandSender, String label, String... args) {
        return runSubCommand(commandSender, args).getResult().isSuccessful();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String label, String[] strings) {
        return Collections.emptyList();
    }
}
