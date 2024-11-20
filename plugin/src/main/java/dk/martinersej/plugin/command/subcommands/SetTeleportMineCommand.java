package dk.martinersej.plugin.command.subcommands;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

public class SetTeleportMineCommand extends SubCommand {

    public SetTeleportMineCommand() {
        super(new String[]{"settpmine", "setteleport"}, "Set a teleport location for a mine", "settpmine <mine> [<x> <y> <z>]", "flawmines.settpmine");
        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 1) { // check if player has defined a mine
            return Result.wrongUsage(this);
        }

        Player player = (Player) sender;
        // Create mine
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());
        // check if mine not exists
        Mine mine = mineManager.getMine(args[0]);
        if (mine == null) {
            return Result.error(this, "§cMine not found!");
        }

        // check if player has defined x, y, z
        BlockVector teleportLocation;
        if (args.length < 4) {
            // use player location
            teleportLocation = player.getLocation().toVector().toBlockVector();
        } else {
            teleportLocation = new BlockVector();
            try {
                teleportLocation.setX(Double.parseDouble(args[1]));
                teleportLocation.setY(Double.parseDouble(args[2]));
                teleportLocation.setZ(Double.parseDouble(args[3]));
            } catch (NumberFormatException e) {
                return Result.error(this, "§cInvalid coordinates!");
            }
        }

        mineManager.editMine(mine, mine_ -> mine_.setTeleportLocation(teleportLocation));
        sender.sendMessage("§aTeleport location set for mine: " + mine.getName());

        return Result.success(this);
    }
}
