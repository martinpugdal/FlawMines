package dk.martinersej.api.worldedit;

import dk.martinersej.api.FlawMinesInterface;
import org.bukkit.entity.Player;

public abstract class WorldEditInterface {

    protected final FlawMinesInterface pluginInterface;

    public WorldEditInterface(FlawMinesInterface pluginInterface) {
        this.pluginInterface = pluginInterface;
    }

    public abstract WorldEditSelection getPlayerSelection(Player player);
}
