package dk.martinersej.plugin.events;

import dk.martinersej.plugin.mine.Mine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MineResetEvent extends Event {


    private static final HandlerList HANDLERS = new HandlerList();

    private final Mine mine;

    public MineResetEvent(Mine mine) {
        this.mine = mine;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Mine getMine() {
        return mine;
    }
}
