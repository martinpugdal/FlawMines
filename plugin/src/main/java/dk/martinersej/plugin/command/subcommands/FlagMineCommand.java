package dk.martinersej.plugin.command.subcommands;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.MineProperty;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FlagMineCommand extends SubCommand {

    public FlagMineCommand() {
        super(new String[]{"editmine", "fm"}, "Flag a mine", "editmine <name> <property> <value>", "mines.property");

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

    @Override
    public List<String> tabComplete(CommandSender commandSender, String label, String[] strings) {
        MineProperty[] properties = MineProperty.values();
        String[] propertyNames = new String[properties.length];
        for (int i = 0; i < properties.length; i++) {
            propertyNames[i] = properties[i].name().toLowerCase();
        }
        return filterStartingWith(strings[strings.length - 1], propertyNames);
    }
}
