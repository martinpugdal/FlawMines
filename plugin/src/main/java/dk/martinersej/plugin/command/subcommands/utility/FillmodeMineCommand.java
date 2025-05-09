package dk.martinersej.plugin.command.subcommands.utility;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FillmodeMineCommand extends SubCommand {

    public FillmodeMineCommand() {
        super(new String[]{"fillmode", "fm"}, "Fillmode a mine", "fillmode <mine> <boolean>", "flawmines.fillmode");

        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
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
        boolean fillmode;
        try {
            fillmode = Boolean.parseBoolean(args[1]);
        } catch (NumberFormatException e) {
            return Result.error(this, "§cInvalid boolean!");
        }

        mineManager.editMine(mine, mine_ -> mine_.setFillmode(fillmode));
        return Result.success(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
        if (strings.length < 2) {
            String check = strings.length == 0 ? "" : strings[0];
            return filterStartingWith(check, FlawMines.get().getMineManager(((Player) commandSender).getWorld()).getMineNames());
        } else if (strings.length == 2) {
            String check = strings[1];
            ArrayList<String> booleans = new ArrayList<String>() {{
                add("true");
                add("false");
            }};
            return filterStartingWith(check, booleans);
        }

        return new ArrayList<>();
    }
}
