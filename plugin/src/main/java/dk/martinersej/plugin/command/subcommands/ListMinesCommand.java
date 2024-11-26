package dk.martinersej.plugin.command.subcommands;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.MineBlock;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListMinesCommand extends SubCommand {

    public ListMinesCommand() {
        super(new String[] {"listmines", "lm"}, "List all mines", "listmines", "flawmines.listmines");
        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());
        if (mineManager.getMineNames().isEmpty()) {
            return Result.error(this, "§cThere are no mines in this world!");
        }
        player.sendMessage("§6Mines:");
        for (String mine : mineManager.getMineNames()) {
            player.sendMessage("§7- §e" + mine);
        }
        return Result.success(this);
    }
}
