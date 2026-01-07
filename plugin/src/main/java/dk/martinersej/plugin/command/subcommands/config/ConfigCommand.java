package dk.martinersej.plugin.command.subcommands.config;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.config.SettingsManager;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ConfigCommand extends SubCommand {

    public ConfigCommand() {
        super(new String[]{"config"}, "Reload configs", "config reload", "flawmines.config", "flawmines.config.reload");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return Result.wrongUsage(this);
        }

        String action = args[0];

        if (action.equalsIgnoreCase("reload")) {
            SettingsManager.reload(FlawMines.get().getDataFolder());
            sender.sendMessage("§aReloaded configs!");
            return Result.success(this);
        }
        return Result.success(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
        if (strings.length < 1) {
            return filterStartingWith("", new ArrayList<String>() {{
                add("reload");
            }});
        } else if (strings.length == 1) {
            String check = strings[0];
            return filterStartingWith(check, new ArrayList<String>() {{
                add("reload");
            }});
        }
        return new ArrayList<>();
    }
}
