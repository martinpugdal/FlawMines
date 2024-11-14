package dk.martinersej.plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.plugin.database.SQLiteDatabase;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class FlawMines extends JavaPlugin implements Listener {

    // statically available instance
    private static FlawMines instance;

    // general variables
    @Getter
    private WorldEditPlugin worldEdit = null;
    @Getter
    private WorldEditInterface worldEditInterface = null;
    @Getter
    private SQLiteDatabase sqLiteDatabase;

    // mine management
    private final Map<World, MineManager> mineManagers = new HashMap<>();

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // setup WorldEdit
        setupWorldEdit();

        // setup sqLiteDatabase
        sqLiteDatabase = new SQLiteDatabase();

        // setup world listener
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        worldEdit = null;
        worldEditInterface = null;
    }

    private void setupWorldEdit() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if (!(plugin instanceof WorldEditPlugin) || !plugin.isEnabled()) {
            getLogger().severe("WorldEdit not found, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // get WorldEdit version
        worldEdit = (WorldEditPlugin) plugin;
        final String rawVersion = worldEdit.getDescription().getVersion();

        final String weVersion = rawVersion.split("", 1)[0]; // Get the first character of the version string

        // Load WorldEdit
        try {
            Class<?> clazz = Class.forName("dk.martinersej.api.worldedit.handlers" + rawVersion);
            // Check if we have a valid WorldEditInterface implementation
            if (WorldEditInterface.class.isAssignableFrom(clazz)) { // Make sure it actually implements WorldEditInterface
                worldEditInterface = (WorldEditInterface) clazz.getConstructor(FlawMinesInterface.class).newInstance(this);
            } else {
                throw new Exception(); // Throw an exception if it doesn't
            }
        } catch (Exception ignored) {
            getLogger().severe("WorldEdit version " + weVersion + " is not supported, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public static FlawMines get() {
        return instance;
    }

    public MineManager getMineManager(World world) {
        return mineManagers.get(world);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        mineManagers.put(world, new MineManager(world));
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        MineManager mineManager = mineManagers.remove(world);
        if (mineManager != null) {
            mineManager.disable();
        }
    }
}
