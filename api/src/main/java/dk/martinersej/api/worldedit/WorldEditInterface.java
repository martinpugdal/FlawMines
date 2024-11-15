package dk.martinersej.api.worldedit;

import dk.martinersej.api.FlawMinesInterface;

public abstract class WorldEditInterface {

    protected final FlawMinesInterface pluginInterface;

    public WorldEditInterface(FlawMinesInterface pluginInterface) {
        this.pluginInterface = pluginInterface;
    }
}
