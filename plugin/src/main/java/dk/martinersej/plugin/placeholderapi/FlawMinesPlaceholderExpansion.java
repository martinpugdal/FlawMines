package dk.martinersej.plugin.placeholderapi;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.placeholderapi.requests.ResetTimeRequest;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FlawMinesPlaceholderExpansion extends PlaceholderExpansion {

    private final FlawMines plugin;
    private final PAPIRequest[] requests;

    public FlawMinesPlaceholderExpansion(FlawMines plugin) {
        this.plugin = plugin;

        // requests
        this.requests = new PAPIRequest[]{
            new ResetTimeRequest()
        };
    }

    @Override
    public @NotNull String getAuthor() {
        return String.valueOf(plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        for (PAPIRequest request : requests) {
            if (request.matches(params)) {
                return request.onRequest(player, params);
            }
        }
        return null;
    }
}