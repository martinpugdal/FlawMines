package dk.martinersej.plugin.command.subcommands.block;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.MineBlock;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListBlockCommand extends SubCommand {

    public ListBlockCommand() {
        super(new String[] {"listblock", "lb"}, "List all blocks in the mine", "listblock <mine>", "flawmines.listblock");

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

        if (mine.getBlocks().isEmpty()) {
            return Result.error(this, "§cThere are no blocks in this mine!");
        }

        // list blocks
        StringBuilder stringBuilder = new StringBuilder();
        for (MineBlock mineBlock : mine.getBlocks()) {
            stringBuilder.append(mineBlock.getBlockData().getItemType().name()).append(": ").append(mineBlock.getWeight()).append("\n");
        }
        sender.sendMessage(stringBuilder.toString());

        return Result.success(this);
    }
}
