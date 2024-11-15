package dk.martinersej.plugin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.martinersej.plugin.mine.Mine;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MineManager {

    private final FlawMines plugin = FlawMines.get();
    private final MineController mineController = plugin.getMineController();

    private final World world;
    private final Map<ProtectedRegion, Mine> mines = new HashMap<>();

    public MineManager(World world) {
        this.world = world;
    }

    void enable() {
        plugin.getLogger().info("Loading mines from world: " + world.getName());

        String query = " SELECT * FROM mines WHERE world = ? " +
            " LEFT JOIN mineBlocks mB ON mines.id = mB.mineId " +
            " LEFT JOIN mineEnvironments mE ON mines.id = mE.mineId; " ;

        plugin.getMineController().async((connection) -> {
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, world.getName());

                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String id = resultSet.getString("id"); // mine id = name
                    String regionName = resultSet.getString("region");

                    // worldguard support for getting a region
                    ProtectedRegion region = plugin.getWorldGuardInterface().getRegionManager(world).getRegion(regionName);
                    if (region == null) {
                        plugin.getLogger().warning("Mine region not found: " + regionName);
                        continue;
                    }

                    Mine mine = new Mine(id, region, world);
                    mines.put(region, mine);

                    // load the blocks and environments for the mine
                    do {
                        // load the blocks stuff here for mine

                        // load the environment stuff here for mine too
                    } while (resultSet.next());

                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void disable() {
        //todo: implement so its saving mines to database and then unloading them.
        for (Mine mine : mines.values()) {
            mineController.saveMine(mine);
        }
    }

    public void createMine(ProtectedRegion region, String name) {
        Mine mine = new Mine(name, region, world);
        mines.put(region, mine);
        mineController.createMine(mine.getName(), mine.getWorld(), region.getId());


        plugin.getLogger().info("Mine created: " + name + " in region: " + region.getId());
    }
}
