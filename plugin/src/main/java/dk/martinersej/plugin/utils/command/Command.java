package dk.martinersej.plugin.utils.command;

import dk.martinersej.plugin.FlawMines;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Command extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();
    private final String[] permissions;
    private boolean playerOnly = false;

    // Constructors
    public Command(String name, String... aliases) {
        super(name, "", "", Arrays.asList(aliases));
        this.permissions = new String[0];
    }

    public Command(String name, String[] aliases, String description, String usage, String[] permissions, String permissionMessage) {
        this(name, aliases, description, usage, permissions);
        setPermissionMessage(permissionMessage);
    }

    public Command(String name, String[] aliases, String description, String usage, String permission, String permissionMessage) {
        this(name, aliases, description, usage, new String[]{permission}, permissionMessage);
    }

    public Command(String name, String[] aliases, String description, String usage) {
        this(name, aliases, description, usage, new String[0]);
    }

    public Command(String name, String[] aliases, String description, String usage, String... permissions) {
        super(name, description, usage, Arrays.asList(aliases));
        this.permissions = permissions;
        setPermission(permissions.length == 0 ? null : permissions[0]);
        setPermissionMessage("§cYou do not have permission to use this command!");
    }

    // Subcommand Management
    protected void addSubCommand(SubCommand subCommand) {
        this.subCommands.add(subCommand);
    }

    protected boolean hasSubCommands() {
        return !this.subCommands.isEmpty();
    }

    protected SubCommand getSubCommandFromAlias(String alias) {
        for (SubCommand subCommand : this.subCommands) {
            if (subCommand.containsAlias(alias)) {
                return subCommand;
            }
        }
        return null;
    }

    protected ArrayList<SubCommand> getSubCommands() {
        return this.subCommands;
    }

    // Permission & Player checks
    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    protected boolean isPlayer(CommandSender sender, String notPlayerMessage) {
        if (this.isPlayer(sender)) {
            return true;
        }
        sender.sendMessage(notPlayerMessage);
        return false;
    }

    protected boolean hasPermission(CommandSender sender, String... permissions) {
        for (String perm : permissions) {
            if (sender.hasPermission(perm)) {
                return true;
            }
        }
        return permissions.length == 0;
    }

    public boolean isPlayerOnly() {
        return this.playerOnly; // If playerOnly is true, only players are allowed
    }

    public void setPlayerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
    }

    public boolean isConsoleAllowed() {
        return !this.playerOnly; // If playerOnly is true, console is not allowed
    }

    // Handling Allowed SubCommands for TabCompletion
    public List<String> getAllowedSubCommands(CommandSender commandSender, String[] aliases) {
        ArrayList<String> allowedSubCommands = new ArrayList<>();
        for (SubCommand subCommand : this.getSubCommands()) {
            if (hasPermission(commandSender, subCommand.getPermissions()) && aliases.length == 1) {
                if (subCommand.containsAlias(aliases[0])) {
                    allowedSubCommands.add(subCommand.getAliases().get(0));
                } else {
                    for (String alias : subCommand.getAliases()) {
                        if (alias.startsWith(aliases[0])) {
                            allowedSubCommands.add(alias);
                            break;
                        }
                    }
                }
            }
        }
        return allowedSubCommands;
    }

    // Handling Allowed SubCommands for finding the correct SubCommand based on the alias
    public SubCommand getAllowedSubCommand(CommandSender commandSender, String alias) {
        for (SubCommand subCommand : this.getSubCommands()) {
            if (hasPermission(commandSender, subCommand.getPermissions()) && subCommand.containsAlias(alias)) {
                return subCommand;
            }
        }
        return null;
    }

    // Abstract run method for implementation
    abstract public boolean run(CommandSender commandSender, String label, String... args);

    @Override
    public boolean execute(CommandSender commandSender, String label, String[] strings) {
        if (!this.hasPermission(commandSender, this.permissions)) {
            return testPermission(commandSender);
        }
        if (this.isPlayerOnly() && !this.isPlayer(commandSender)) {
            commandSender.sendMessage("§cOnly players can execute this command!");
            return true;
        }
        if (!this.isConsoleAllowed() && !this.isPlayer(commandSender)) {
            commandSender.sendMessage("§cOnly console can execute this command!");
            return true;
        }
        boolean successed = false;
        try {
            successed = this.run(commandSender, label, strings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!successed) {
            commandSender.sendMessage(this.getUsage());
        }
        return successed;
    }

    protected CommandResult runSubCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return new CommandResult(null, Result.NO_SUB_COMMAND_FOUND);
        }
        SubCommand subCommand = getSubCommandFromAlias(args[0]);
        if (subCommand == null) {
            return new CommandResult(null, Result.NO_SUB_COMMAND_FOUND);
        }
        if (!this.hasPermission(sender, subCommand.getPermissions())) {
            return new CommandResult(subCommand, Result.NO_PERMISSION);
        }
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        CommandResult result = subCommand.execute(sender, newArgs);
        if (result == null)
            result = Result.error(subCommand);

        return result;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String label, String[] strings) {
        // check if args[0] is a subcommand, then tabcomplete that subcommand
        if (strings.length > 0) {
            SubCommand subCommand = getAllowedSubCommand(commandSender, strings[0]);
            if (subCommand != null) {
                return subCommand.onTabComplete(commandSender, Arrays.copyOfRange(strings, 1, strings.length));
            }
        }

        return defaultTabComplete(commandSender, strings);
    }

    protected List<String> defaultTabComplete(CommandSender commandSender, String[] strings) {
        if (strings.length == 1) {
            return getAllowedSubCommands(commandSender, strings);
        } else {
            return new ArrayList<>();
        }
    }

    protected List<String> filterStartingWith(String string, String[] values) {
        List<String> completions = new ArrayList<>();
        for (String s : values) {
            if (s.startsWith(string)) {
                completions.add(s);
            }
        }
        return completions;
    }

    protected List<String> filterStartingWith(String string, List<String> values) {
        List<String> completions = new ArrayList<>();
        for (String s : values) {
            if (s.startsWith(string)) {
                completions.add(s);
            }
        }
        return completions;
    }

    public String getDescription() {
        return this.description;
    }

    public String[] getPermissions() {
        return this.permissions;
    }

    @Override
    public Plugin getPlugin() {
        return FlawMines.get();
    }
}
