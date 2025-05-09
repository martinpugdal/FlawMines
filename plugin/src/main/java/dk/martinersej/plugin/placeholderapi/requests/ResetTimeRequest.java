package dk.martinersej.plugin.placeholderapi.requests;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.MineManager;
import dk.martinersej.plugin.config.Messages;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.environments.ScheduledEnvironment;
import dk.martinersej.plugin.placeholderapi.PAPIRequest;
import dk.martinersej.plugin.utils.TimeUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.util.List;

public class ResetTimeRequest extends PAPIRequest {

    public ResetTimeRequest() {
        super("reset_time_(\\S+)"); // reset_time_<mine> (include all characters)
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        final FlawMines plugin = FlawMines.get();

        // player needs to be online because the mine is in the player's world
        if (!player.isOnline()) {
            return null;
        }

        World world = player.getPlayer().getWorld();
        MineManager mineManager = plugin.getMineManager(world);
        if (mineManager == null) {
            return null;
        }

        // get the mine by the regex group
        String mineName = getRegexGroup(params, 1); // the mine name
        if (mineName == null) {
            return null;
        }

        // get the mine and get the time left
        Mine mine = mineManager.getMine(mineName);
        if (mine == null) {
            return null;
        }

        // has to be a scheduled environment
        List<Environment> environments = mine.getEnvironments(ScheduledEnvironment.class);
        if (environments.isEmpty()) {
            return null;
        }

        // get the shortest time left
        int shortestTimeLeft = Integer.MAX_VALUE;
        for (Environment environment : environments) {
            ScheduledEnvironment scheduledEnvironment = (ScheduledEnvironment) environment;
            int timeLeft = scheduledEnvironment.getTimeLeft();
            if (timeLeft < shortestTimeLeft && timeLeft >= 0) {
                shortestTimeLeft = timeLeft;
            }
        }

        if (shortestTimeLeft == Integer.MAX_VALUE) {
            return Messages.RESETTING.get();
        }

        return TimeUtils.formatTime(shortestTimeLeft);
    }
}
