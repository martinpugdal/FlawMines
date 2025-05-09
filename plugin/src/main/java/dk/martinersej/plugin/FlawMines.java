package dk.martinersej.plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.api.worldguard.WorldGuardInterface;
import dk.martinersej.plugin.command.BaseCommand;
import dk.martinersej.plugin.config.SettingsManager;
import dk.martinersej.plugin.mine.MineController;
import dk.martinersej.plugin.mine.MineListener;
import dk.martinersej.plugin.mine.MineManager;
import dk.martinersej.plugin.placeholderapi.FlawMinesPlaceholderExpansion;
import dk.martinersej.plugin.utils.TaskUtils;
import dk.martinersej.plugin.utils.command.CommandInjector;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FlawMines extends JavaPlugin implements Listener, FlawMinesInterface {

    // statically available instance
    private static FlawMines instance;
    private static boolean legacy = false;
    // mine management
    private final Map<World, MineManager> mineManagers = new HashMap<>();
    // general variables
    private WorldEditPlugin worldEdit = null;
    private WorldEditInterface worldEditInterface = null;
    private WorldGuardPlugin worldGuard = null;
    private WorldGuardInterface worldGuardInterface = null;
    private CommandInjector commandInjector;

    private MineController mineController;

    public static FlawMines get() {
        return instance;
    }

    public static boolean isLegacy() {
        return legacy;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // setup legacy
        setupLegacyCheck();
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

        // setup PlaceholderAPI
        setupPlaceholderAPI();
        // setup configs
        SettingsManager.init(getDataFolder());

        // setup mine controller
        mineController = new MineController();

        // setup world listener
        getServer().getPluginManager().registerEvents(this, this);

        // setup command injector
        commandInjector = new CommandInjector();
        // register commands
        commandInjector.enableCommand(new BaseCommand(), this);

        // register listeners
        Bukkit.getPluginManager().registerEvents(new MineListener(), this);

        // load all worlds
        TaskUtils.runTaskSync(() -> {
            for (World world : Bukkit.getWorlds()) {
                mineManagers.put(world, new MineManager(world));
                mineManagers.get(world).enable();
            }
        });
    }

    @Override
    public void onDisable() {
        for (MineManager mineManager : mineManagers.values()) {
            mineManager.disable();
        }

        worldEdit = null;
        worldEditInterface = null;
        worldGuard = null;
        worldGuardInterface = null;

        if (commandInjector != null) {
            commandInjector.disableAllCommands();
        }
        commandInjector = null;
    }

    private void setupLegacyCheck() {
        // check version of server
        String version;
        try {
            // in 1.20.5 its relocated, so we can just handle it in the catch if it fails
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            return;
        }
        // check if version is 1.12 or lower
        String[] legacyVersions = {"v1_8", "v1_9", "v1_10", "v1_11", "v1_12"};
        for (String legacyVersion : legacyVersions) {
            if (version.startsWith(legacyVersion)) {
                legacy = true;
                break;
            }
        }
    }

    private void setupPlaceholderAPI() {
        Plugin plugin = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (plugin != null && plugin.isEnabled()) {
            new FlawMinesPlaceholderExpansion(this).register();
        }
    }

    private void setupWorldEdit() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if (!(plugin instanceof WorldEditPlugin) || !plugin.isEnabled()) {
            getLogger().severe("WorldEdit not found");
            return;
        }

        // Get WorldEdit plugin
        worldEdit = (WorldEditPlugin) plugin;

        // Parse WorldEdit version
        String version = worldEdit.getDescription().getVersion(); // Example: "7.3.0"
        String[] versionParts = version.split("\\.");
        int majorVersion = Integer.parseInt(versionParts[0]); // "7"
        int minorVersion = versionParts.length > 1 ? Integer.parseInt(versionParts[1]) : 0; // "3"

        // Find the nearest floor handler
        List<Integer> availableHandlers = new ArrayList<>();
        for (int i = 0; i <= minorVersion; i++) {
            try {
                Class.forName("dk.martinersej.handlers.WorldEdit" + majorVersion + "_" + i);
                availableHandlers.add(i);
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        Class<?> handlerClass = null;
        if (!availableHandlers.isEmpty()) {
            // Use the closest floor version
            int nearestMinor = availableHandlers.get(availableHandlers.size() - 1); // Get the last element
            try {
                handlerClass = Class.forName("dk.martinersej.handlers.WorldEdit" + majorVersion + "_" + nearestMinor);
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            // Fall back to major version if no minors are available
            try {
                handlerClass = Class.forName("dk.martinersej.handlers.WorldEdit" + majorVersion);
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (handlerClass != null && WorldEditInterface.class.isAssignableFrom(handlerClass)) {
            try {
                worldEditInterface = (WorldEditInterface) handlerClass.getConstructor(FlawMinesInterface.class).newInstance(this);
                getLogger().info("WorldEdit version " + version + " is supported with handler " + handlerClass.getName());
            } catch (Exception e) {
                e.printStackTrace();
                getLogger().severe("Error loading WorldEdit handler for version " + version);
                worldEdit = null;
            }
        } else {
            getLogger().severe("WorldEdit version " + version + " is not supported");
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
