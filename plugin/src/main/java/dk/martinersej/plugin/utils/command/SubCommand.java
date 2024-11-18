package dk.martinersej.plugin.utils.command;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public abstract class SubCommand extends Command {

    private final List<String> aliases;

    public SubCommand(String[] aliases, String description, String usage, String permission) {
        super(aliases[0], Arrays.stream(aliases).skip(1).toArray(String[]::new), description, usage, permission);

        this.aliases = Arrays.asList(aliases);
    }

    public SubCommand(String[] aliases, String description, String usage, String... permissions) {
        super(aliases[0], Arrays.stream(aliases).skip(1).toArray(String[]::new), description, usage, permissions);

        this.aliases = Arrays.asList(aliases);
    }

    protected boolean containsAlias(String alias) {
        for (String s : this.aliases) {
            if (s.equalsIgnoreCase(alias)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean run(CommandSender sender, String label, String[] args) {
        return false; // not used in SubCommand
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
        return null; // not used in SubCommand
    }

    public abstract CommandResult execute(CommandSender sender, String[] args);

    public String getUsage(String label) {
        return label + " " + this.getUsage();
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }
}