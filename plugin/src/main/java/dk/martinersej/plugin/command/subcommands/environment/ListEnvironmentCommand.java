package dk.martinersej.plugin.command.subcommands.environment;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListEnvironmentCommand extends SubCommand {

    public ListEnvironmentCommand() {
        super(new String[] {"listenv", "listenvironment"}, "List all environments in a mine", "listenv <mine>", "flawmines.listenv");
        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return Result.wrongUsage(this);
        }

        String mineName = args[0];

        // check for mine existence
        Player player = (Player) sender;
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());
        Mine mine = mineManager.getMine(mineName);
        if (mine == null) {
            return Result.error(this, "§cMine not found!");
        }

        if (mine.getEnvironments().isEmpty()) {
            return Result.error(this, "§cThere are no environments in this mine!");
        }

        // list environments
        StringBuilder stringBuilder = new StringBuilder();
        for (Environment environment : mine.getEnvironments()) {
            String name = environment.getClass().getSimpleName();
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            stringBuilder.append(name).
                append(" (").append(environment.getId()).append(")")
                .append(": ").append(environment.getProgress()).append("%\n");
        }
        sender.sendMessage(stringBuilder.toString());

        return Result.success(this);
    }
}
