package dk.martinersej.plugin.command.subcommands;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveMineCommand extends SubCommand {

    public RemoveMineCommand() {
        super(new String[]{"removemine"}, "Remove a mine", "removemine <name>", "mines.remove");

        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return Result.wrongUsage(this);
        }

        String mineName = args[0];
        Player player = (Player) sender;
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());
        Mine mine = mineManager.getMine(mineName);
        if (mine == null) {
            return Result.error(this, "§cMine not found!");
        }

        boolean deleted = mineManager.deleteMine(mine);
        if (!deleted) {
            return Result.error(this, "§cFailed to delete mine!");
        }

        sender.sendMessage("§aMine removed!");

        return Result.success(this);
    }
}
