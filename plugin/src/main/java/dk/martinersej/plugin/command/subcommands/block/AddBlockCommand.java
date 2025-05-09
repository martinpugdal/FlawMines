package dk.martinersej.plugin.command.subcommands.block;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.MineBlock;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.List;

public class AddBlockCommand extends SubCommand {

    public AddBlockCommand() {
        super(new String[] {"addblock", "ab"}, "Add a block to the mine", "addblock <mine> <block> <weight>", "flawmines.addblock");

        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            return Result.wrongUsage(this);
        }

        String mineName = args[0];
        String[] data = args[1].split(":"); // block:meta if needed
        byte meta = 0;
        try {
            meta = Byte.parseByte(data[1]);
        } catch (ArrayIndexOutOfBoundsException ignored) {} catch (NumberFormatException e) {
            return Result.error(this, "§cInvalid meta!");
        }

        MaterialData materialData = new MaterialData(Material.matchMaterial(data[0]), meta);
        if (materialData.getItemType() == null) {
            return Result.error(this, "§cInvalid block!");
        }
        if (!materialData.getItemType().isBlock()) {
            return Result.error(this, "§cIs not a block!");
        }
        float percentage;
        try {
            percentage = Float.parseFloat(args[2]);
        } catch (NumberFormatException e) {
            return Result.error(this, "§cInvalid weight!");
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
            return Result.error(this, "§cWeight must be a positive number!");
        }

        // add block
        MineBlock mineBlock = mineManager.addBlock(mine, new MineBlock(materialData, percentage));
        // use the mineblock to tell the output what we have as weight

        if (mineBlock != null) {
            sender.sendMessage("§aBlock was found and updated the weight to " + mineBlock.getWeight());
        } else {
            sender.sendMessage("§aBlock was added with a weight of " + percentage);
        }

        return Result.success(this);
    }

    private static final String[] materials;
    static {
        materials = new String[Material.values().length];
        for (int i = 0; i < Material.values().length; i++) {
            materials[i] = Material.values()[i].name();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
        if (strings.length == 1) {
            return FlawMines.get().getMineManager(((Player) commandSender).getWorld()).getMineNames();
        } else if (strings.length == 2) {
            return filterStartingWith(strings[1], materials);
        }

        return super.onTabComplete(commandSender, strings);
    }
}
