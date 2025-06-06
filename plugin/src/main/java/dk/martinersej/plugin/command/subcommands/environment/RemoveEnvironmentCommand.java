package dk.martinersej.plugin.command.subcommands.environment;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveEnvironmentCommand extends SubCommand {

    public RemoveEnvironmentCommand() {
        super(new String[] {"envremove", "removeenv"}, "Remove environment from a mine",  "removeenv <mine> <id>", "flawmines.env.remove");
        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return Result.wrongUsage(this);
        }

        String mineName = args[0];
        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return Result.error(this, "§cInvalid id!");
        }

        // check for mine existence
        Player player = (Player) sender;
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());
        Mine mine = mineManager.getMine(mineName);
        if (mine == null) {
            return Result.error(this, "§cMine not found!");
        }
        Environment environment = mine.getEnvironment(id);
        if (environment == null) {
            return Result.error(this, "§cEnvironment not found!");
        }

        mineManager.removeEnvironment(mine, environment);
        sender.sendMessage("§aEnvironment removed!");

        return Result.success(this);
    }
}
