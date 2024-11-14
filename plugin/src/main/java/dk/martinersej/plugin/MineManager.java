package dk.martinersej.plugin;

import dk.martinersej.plugin.mine.Mine;
import org.bukkit.World;
import org.bukkit.util.BlockVector;

import java.util.HashMap;
import java.util.Map;

public class MineManager {

    private final FlawMines plugin = FlawMines.get();
    private final World world;

    private final Map<BlockVector, Mine> mines = new HashMap<>();

    public MineManager(World world) {
        this.world = world;
    }

    private void enable() {
        //todo: implement so its loading mines fom database.
    }


    public void disable() {
        //todo: implement so its saving mines to database and then unloading them.
    }
}
