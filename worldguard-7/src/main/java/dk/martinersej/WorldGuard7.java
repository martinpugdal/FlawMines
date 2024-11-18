package dk.martinersej;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dk.martinersej.api.FlawMinesInterface;
import dk.martinersej.api.worldguard.WorldGuardInterface;
import org.bukkit.World;

public class WorldGuard7 extends WorldGuardInterface {

    public WorldGuard7(FlawMinesInterface pluginInterface) {
        super(pluginInterface);
    }

    @Override
    public RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }
}