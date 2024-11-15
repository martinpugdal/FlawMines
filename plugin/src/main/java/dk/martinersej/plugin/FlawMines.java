package dk.martinersej.plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.api.worldguard.WorldGuardInterface;
import dk.martinersej.plugin.command.TestCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class FlawMines extends JavaPlugin implements Listener, FlawMinesInterface {

    // statically available instance
    private static FlawMines instance;
    // mine management
    private final Map<World, MineManager> mineManagers = new HashMap<>();
    // general variables
    private WorldEditPlugin worldEdit = null;
    private WorldEditInterface worldEditInterface = null;
    private WorldGuardPlugin worldGuard = null;
    private WorldGuardInterface worldGuardInterface = null;

    private MineController mineController;

    public static FlawMines get() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // setup WorldEdit
        setupWorldEdit();
        // setup WorldGuard
        setupWorldGuard();

        // check if WorldEdit and WorldGuard is enabled
        if (worldEdit == null || worldGuard == null) {
            getLogger().severe("WorldEdit or WorldGuard is not enabled, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // create the plugin folder if it doesn't exist
        if (!getDataFolder().exists())
            new File(getDataFolder().getAbsolutePath()).mkdirs();

        // setup mine controller
        mineController = new MineController();

        // setup world listener
        getServer().getPluginManager().registerEvents(this, this);

        // setup test command
        getCommand("test").setExecutor(new TestCommand());

        for (World world : Bukkit.getWorlds()) {
            MineManager manager = mineManagers.put(world, new MineManager(world));
            if (manager != null)
                manager.enable();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        worldEdit = null;
        worldEditInterface = null;
        worldGuard = null;
        worldGuardInterface = null;
    }

    private void setupWorldEdit() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if (!(plugin instanceof WorldEditPlugin) || !plugin.isEnabled()) {
            getLogger().severe("WorldEdit not found");
            return;
        }

        // Get WorldEdit version
        worldEdit = (WorldEditPlugin) plugin;

        // Extract major version
        final String weVersion = worldEdit.getDescription().getVersion().charAt(0) + "";

        // Load WorldEdit
        try {
            Class<?> clazz = Class.forName("dk.martinersej.handlers.WorldEdit" + weVersion);
            // Check if we have a valid WorldEditInterface implementation
            if (WorldEditInterface.class.isAssignableFrom(clazz)) { // Make sure it actually implements WorldEditInterface
                getLogger().info("WorldEdit version " + weVersion + " is supported");
                worldEditInterface = (WorldEditInterface) clazz.getConstructor(FlawMinesInterface.class).newInstance(this);
            } else {
                throw new Exception(); // Throw an exception if it doesn't
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            getLogger().severe("WorldEdit version " + weVersion + " is not supported");
            worldEdit = null;
        }
    }

    private void setupWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (!(plugin instanceof WorldGuardPlugin) || !plugin.isEnabled()) {
            getLogger().severe("WorldGuard not found");
            return;
        }
        // get WorldGuard version
        worldGuard = (WorldGuardPlugin) plugin;

        // Extract major version
        final String wgVersion = worldGuard.getDescription().getVersion().charAt(0) + "";

        // Load WorldGuard
        try {
            Class<?> clazz = Class.forName("dk.martinersej.handlers.WorldGuard" + wgVersion);
            // Check if we have a valid WorldGuardInterface implementation
            if (WorldGuardInterface.class.isAssignableFrom(clazz)) { // Make sure it actually implements WorldGuardInterface
                worldGuardInterface = (WorldGuardInterface) clazz.getConstructor(FlawMinesInterface.class).newInstance(this);
            } else {
                throw new Exception(); // Throw an exception if it doesn't
            }
        } catch (Exception ignored) {
            getLogger().severe("WorldGuard version " + wgVersion + " is not supported");
            worldGuard = null;
        }
    }

    public MineManager getMineManager(World world) {
        return mineManagers.get(world); // should never be null because of the world listener
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        MineManager manager = mineManagers.put(world, new MineManager(world));
        if (manager != null) {
            manager.enable();
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        MineManager mineManager = mineManagers.remove(world);
        if (mineManager != null) {
            mineManager.disable();
        }
    }

    public WorldEditInterface getWorldEditInterface() {
        return worldEditInterface;
    }

    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    public WorldGuardInterface getWorldGuardInterface() {
        return worldGuardInterface;
    }

    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }

    public MineController getMineController() {
        return mineController;
    }
}
