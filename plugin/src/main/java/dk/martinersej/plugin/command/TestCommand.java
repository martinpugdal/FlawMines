package dk.martinersej.plugin.command;

import dk.martinersej.api.worldedit.WorldEditSelection;
import dk.martinersej.plugin.FlawMines;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        FlawMines plugin = FlawMines.getInstance();

        // get selection
        Player player = (Player) commandSender;
        WorldEditSelection playerSelection = plugin.getWorldEditInterface().getPlayerSelection(player);

        if (playerSelection == null) {
            player.sendMessage("You need to make a WorldEdit selection first.");
            return true;
        }

        player.sendMessage("You have selected a region from " + playerSelection.getMinimumLocation() + " to " + playerSelection.getMaximumLocation());


        return true;
    }
}
