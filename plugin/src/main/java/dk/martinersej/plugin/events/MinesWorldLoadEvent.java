package dk.martinersej.plugin.events;

import dk.martinersej.plugin.mine.Mine;
import net.royawesome.jlibnoise.module.combiner.Min;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class MinesWorldLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Mine> mines;
    private final World world;

    public MinesWorldLoadEvent(List<Mine> mines, World world) {
        this.mines = mines;
        this.world = world;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public List<Mine> getMines() {
        return mines;
    }

    public World getWorld() {
        return world;
    }
}
