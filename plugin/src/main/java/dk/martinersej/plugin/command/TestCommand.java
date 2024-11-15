package dk.martinersej.plugin.command;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        FlawMines plugin = FlawMines.get();

        // get region by argument 1
        Player player = (Player) commandSender;
        // selected region
        String region = strings[0];
        ProtectedRegion region1 = plugin.getWorldGuardInterface().getRegionManager(player.getWorld()).getRegion(region);
        if (region1 == null) {
            player.sendMessage("Region not found: " + region);
            return true;
        }

        String mineName = strings[1];
        if (mineName == null) {
            player.sendMessage("Mine name not found");
            return true;
        }

        MineManager mineManager = plugin.getMineManager(player.getWorld());
        if (mineManager == null) {
            player.sendMessage("Mine manager not found");
            return true;
        }

        // create mine with region and argument 2 is the name of the mine
        plugin.getMineManager(player.getWorld()).createMine(region1, strings[1]);
        player.sendMessage("Mine created: " + mineName);
        return true;
    }
}
