package dk.martinersej.plugin.command.subcommands.utility;

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
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class MigrateCommand extends SubCommand {

    private final List<Plugin> supportedPlugins = new ArrayList<>();

    public MigrateCommand() {
        super(new String[] {"migrate", "import"}, "Import a mine from other plugins", "migrate <plugin> <mine> [<rename>]", "flawmines.migrate");


        // fetch the supported plugins for migration
        Plugin mineResetLite = FlawMines.get().getServer().getPluginManager().getPlugin("MineResetLite");
        if (mineResetLite != null && mineResetLite.isEnabled()) {
            supportedPlugins.add(mineResetLite);
        }
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return Result.success(this);
    }
}
