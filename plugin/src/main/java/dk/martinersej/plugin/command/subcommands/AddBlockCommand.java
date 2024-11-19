package dk.martinersej.plugin.command.subcommands;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.MineBlock;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

public class AddBlockCommand extends SubCommand {

    public AddBlockCommand() {
        super(new String[] {"addblock", "ab"}, "Add a block to the mine", "addblock <mine> <block> <percentage>", "flawmines.addblock");

        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            return Result.wrongUsage(this);
        }

        String mineName = args[0];
        String[] data = args[1].split(":"); // block:meta if needed
        Material material = Material.matchMaterial(data[0]);
        byte meta = 0;
        try {
            meta = Byte.parseByte(data[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {}
        MaterialData materialData = new MaterialData(Material.matchMaterial(data[0]), meta);
        if (materialData.getItemType() == null) {
            return Result.error(this, "§cInvalid block!");
        }
        if (!materialData.getItemType().isBlock()) {
            return Result.error(this, "§cIs not a block!");
        }
        float percentage = -1;
        try {
            percentage = Float.parseFloat(args[2]);
        } catch (NumberFormatException e) {
            return Result.error(this, "§cInvalid percentage!");
        }

        // check for mine existence
        Player player = (Player) sender;
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());
        Mine mine = mineManager.getMine(mineName);
        if (mine == null) {
            return Result.error(this, "§cMine not found!");
        }

        // check for percentage
        if (percentage < 0) {
            return Result.error(this, "§cPercentage must be a positive number!");
        }

        // add block
        MineBlock mineBlock = mineManager.addBlock(mine, new MineBlock(material, percentage));
        // use the mineblock to tell the output what we have as percentage

        if (mineBlock != null) {
            sender.sendMessage("§aBlock was found and updated the percentage to " + mineBlock.getPercentage());
        } else {
            sender.sendMessage("§aBlock was added with a percentage of " + percentage);
        }

        return Result.success(this);
    }
}
