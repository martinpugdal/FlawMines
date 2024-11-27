package dk.martinersej.plugin.placeholderapi.requests;

import dk.martinersej.plugin.placeholderapi.PAPIRequest;
import org.bukkit.OfflinePlayer;

public class ResetTimeRequest extends PAPIRequest {

    public ResetTimeRequest() {
        super("reset_time", "reset_time_(\\w+)"); // reset_time_<mine_name>
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        // todo: implement
        return "Not implemented yet!";
    }
}
