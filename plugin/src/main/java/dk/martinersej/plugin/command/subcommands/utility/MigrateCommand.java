package dk.martinersej.plugin.command.subcommands.utility;

import com.koletar.jj.mineresetlite.MineResetLite;
import com.koletar.jj.mineresetlite.SerializableBlock;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.MineBlock;
import dk.martinersej.plugin.mine.environment.EnvironmentType;
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
import java.util.Map;

public class MigrateCommand extends SubCommand {

    private final List<Plugin> supportedPlugins = new ArrayList<>();

    public MigrateCommand() {
        super(new String[] {"migrate", "import"}, "Import a mine from other plugins", "migrate <plugin> <mine>", "flawmines.migrate");

        setPlayerOnly(true);

        // fetch the supported plugins for migration
        Plugin mineResetLite = FlawMines.get().getServer().getPluginManager().getPlugin("MineResetLite");
        if (mineResetLite != null && mineResetLite.isEnabled()) {
            supportedPlugins.add(mineResetLite);
        }
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return Result.wrongUsage(this);
        }

        String pluginName = args[0];
        if (supportedPlugins.stream().noneMatch(plugin -> plugin.getName().equalsIgnoreCase(pluginName))) {
            return Result.error(this, "The plugin " + pluginName + " is not supported for migration");
        }
        MineResetLite mineResetLite = (MineResetLite) supportedPlugins.stream().filter(plugin -> plugin.getName().equalsIgnoreCase(pluginName)).findFirst().get();

        com.koletar.jj.mineresetlite.Mine mine = mineResetLite.mines.stream().filter(m -> m.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);
        if (mine == null) {
            return Result.error(this, "The mine " + args[1] + " does not exist in the plugin " + pluginName);
        }
        Player player = (Player) sender;
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());

        List<MineBlock> newBlocks = new ArrayList<>();
        Map<SerializableBlock, Double> blocks = mine.getComposition();

        for (Map.Entry<SerializableBlock, Double> entry : blocks.entrySet()) {
            String materialString = entry.getKey().getBlockId();
            byte data = entry.getKey().getData();
            double weight = entry.getValue() * 10;
            // get material by id and data
            Material material = Material.matchMaterial(materialString);
            MaterialData materialData = new MaterialData(material, data);
            newBlocks.add(new MineBlock(materialData, (float) weight));
        }

        // get region with the mine name
        ProtectedRegion region = FlawMines.get().getWorldGuardInterface().getRegionManager(player.getWorld()).getRegion(args[1]);
        if (region == null) {
            return Result.error(this, "The region " + args[1] + " does not exist in the world " + player.getWorld().getName());
        }

        // create the mine
        Mine mine1 = mineManager.createMine(region, args[1]);
        if (mine1 == null) {
            return Result.error(this, "The mine " + args[1] + " already exists");
        }
        mineManager.setBlocks(mine1, newBlocks);

        int resetDelay = mine.getResetDelay();
        if (resetDelay > 0) {
            resetDelay *= 60; // convert minutes to seconds
            mineManager.addEnvironment(mine1, EnvironmentType.SCHEDULED, new Object[] {resetDelay});
        }

        player.sendMessage("The mine " + args[1] + " has been imported from " + mineResetLite.getName());
        return Result.success(this);
    }
}
