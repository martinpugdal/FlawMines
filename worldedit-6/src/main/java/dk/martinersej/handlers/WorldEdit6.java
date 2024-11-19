package dk.martinersej.handlers;

import com.sk89q.worldedit.bukkit.selections.Selection;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.api.worldedit.WorldEditSelection;
import org.bukkit.entity.Player;

public class WorldEdit6 extends WorldEditInterface {

    public WorldEdit6(FlawMinesInterface pluginInterface) {
        super(pluginInterface);
    }

    @Override
    public WorldEditSelection getPlayerSelection(Player player) {
        Selection selection = pluginInterface.getWorldEdit().getSelection(player);
        if (selection == null) {
            return null;
        }
        return new WorldEditSelection(
            player.getWorld(),
            selection.getMinimumPoint(),
            selection.getMaximumPoint()
        );
    }
}