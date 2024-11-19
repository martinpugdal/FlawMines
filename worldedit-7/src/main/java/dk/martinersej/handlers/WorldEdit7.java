package dk.martinersej.handlers;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.api.worldedit.WorldEditSelection;
import org.bukkit.entity.Player;

public class WorldEdit7 extends WorldEditInterface {

    public WorldEdit7(FlawMinesInterface pluginInterface) {
        super(pluginInterface);
    }

    @Override
    public WorldEditSelection getPlayerSelection(Player player) {
        try {
            Region region = pluginInterface.getWorldEdit().getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
            return new WorldEditSelection(
                player.getWorld(),
                BukkitAdapter.adapt(player.getWorld(), region.getMinimumPoint()),
                BukkitAdapter.adapt(player.getWorld(), region.getMaximumPoint())
            );
        } catch (IncompleteRegionException e) {
            return null;
        }
    }
}