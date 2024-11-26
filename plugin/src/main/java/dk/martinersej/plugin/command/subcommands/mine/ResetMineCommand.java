package dk.martinersej.plugin.command.subcommands.mine;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetMineCommand extends SubCommand {

    public ResetMineCommand() {
        super(new String[]{"resetmine"}, "Reset a mine", "resetmine <mine>", "flawmines.resetmine");

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
        mine.reset();

        sender.sendMessage("§aMine reset!");

        return Result.success(this);
    }
}
