package dk.martinersej.plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldedit.WorldEditInterface;
import dk.martinersej.plugin.database.SQLiteDatabase;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class FlawMines extends JavaPlugin {

    // statically available instance
    @Getter
    private static FlawMines instance;

    // general variables
    @Getter
    private WorldEditPlugin worldEdit = null;
    @Getter
    private WorldEditInterface worldEditInterface = null;
    @Getter
    private SQLiteDatabase database;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // setup WorldEdit
        setupWorldEdit();

        // setup database
        database = new SQLiteDatabase();
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
}
