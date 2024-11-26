package dk.martinersej.plugin.command.subcommands.mine;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.api.worldedit.WorldEditSelection;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CreateMineCommand extends SubCommand {

    public CreateMineCommand() {
        super(new String[]{"createmine", "cm"}, "Create a new mine", "createmine <name> [<region>]", "mines.create");

        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        // args = ["name", "region"]
        if (args.length < 1) {
            return Result.wrongUsage(this);
        }


        Player player = (Player) sender;
        // Create mine
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());
        // check if mine already exists
        if (mineManager.getMine(args[0]) != null) {
            return Result.error(this, "§cA mine with that name already exists!");
        }

        // check if player has defined a region
        String regionName = args.length > 1 ? args[1] : null;
        ProtectedRegion region;
        if (regionName != null) {
            region = FlawMines.get().getWorldGuardInterface().getRegionManager(player.getWorld()).getRegion(regionName);
            if (region == null) {
                return Result.error(this, "§cRegion not found!");
            }
        } else {
            // check if player has selected a region
            WorldEditSelection playerSelection = FlawMines.get().getWorldEditInterface().getPlayerSelection(player);
            if (playerSelection == null) {
                return Result.error(this, "§cYou need to select a region!");
            }

            // remove region if it already exists
            if (FlawMines.get().getWorldGuardInterface().getRegionManager(player.getWorld()).hasRegion(args[0])) {
                FlawMines.get().getWorldGuardInterface().getRegionManager(player.getWorld()).removeRegion(args[0]);
            }

            // create region
            Vector min = playerSelection.getMinimumLocation().toVector();
            Vector max = playerSelection.getMaximumLocation().toVector();
            region = FlawMines.get().getWorldGuardInterface().createProtectedCuboidRegion(args[0], min, max);
            FlawMines.get().getWorldGuardInterface().getRegionManager(player.getWorld()).addRegion(region);
        }
        Mine mine = mineManager.createMine(region, args[0]);
        if (mine == null) {
            return Result.error(this, "§cFailed to create mine!");
        }

        sender.sendMessage("§aMine created!");
        return Result.success(this);
    }
}
