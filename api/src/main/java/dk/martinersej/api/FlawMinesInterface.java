package dk.martinersej.api;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public interface FlawMinesInterface {

   WorldEditPlugin getWorldEdit();

   WorldGuardPlugin getWorldGuard();
}
