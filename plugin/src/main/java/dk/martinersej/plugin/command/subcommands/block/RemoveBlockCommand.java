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

public class RemoveBlockCommand extends SubCommand {

    public RemoveBlockCommand() {
        super(new String[]{"removeblock", "rb"}, "Remove a block", "removeblock <mine> <block>", "flawmines.removeblock");

        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
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

        // check for mine existence
        Player player = (Player) sender;
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());
        Mine mine = mineManager.getMine(mineName);
        if (mine == null) {
            return Result.error(this, "§cMine not found!");
        }
        MineBlock block = mine.getBlock(materialData);
        if (block == null) {
            return Result.error(this, "§cBlock not found!");
        }

        mineManager.removeBlock(mine, block);
        sender.sendMessage("§aBlock removed!");

        return Result.success(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
        if (strings.length == 1) {
            return FlawMines.get().getMineManager(((Player) commandSender).getWorld()).getMineNames();
        } else if (strings.length == 2) {
            MineManager mineManager = FlawMines.get().getMineManager(((Player) commandSender).getWorld());
            Mine mine = mineManager.getMine(strings[0]);
            if (mine != null) {
                List<MineBlock> blocks = mine.getBlocks();
                String[] materials = new String[blocks.size()];
                for (int i = 0; i < blocks.size(); i++) {
                    materials[i] = blocks.get(i).getBlockData().getItemType().name();
                }
                return filterStartingWith(strings[1], materials);
            }
        }

        return super.onTabComplete(commandSender, strings);
    }
}
