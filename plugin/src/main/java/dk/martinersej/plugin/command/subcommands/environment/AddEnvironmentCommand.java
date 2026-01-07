package dk.martinersej.plugin.command.subcommands.environment;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.mine.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.EnvironmentType;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddEnvironmentCommand extends SubCommand {

    public AddEnvironmentCommand() {
        super(new String[] {"envadd", "addenv"}, "Create a new environment in a mine",  "addenv <mine> <type> <value(s)>", "flawmines.env.create");
        setPlayerOnly(true);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            return Result.wrongUsage(this);
        }

        String mineName = args[0];

        // check for mine existence
        Player player = (Player) sender;
        MineManager mineManager = FlawMines.get().getMineManager(player.getWorld());
        Mine mine = mineManager.getMine(mineName);
        if (mine == null) {
            return Result.error(this, "§cMine not found!");
        }

        EnvironmentType type = EnvironmentType.fromString(args[1].toUpperCase());
        if (type == null) {
            return Result.error(this, "§cInvalid environment type!");
        }

        Object[] values = new Object[args.length - 2];
        System.arraycopy(args, 2, values, 0, args.length - 2);

        Class<?>[] valueTypes = type.getValues().toArray(new Class<?>[0]);
        for (int i = 0; i < values.length; i++) {
            try {
                if (valueTypes[i] == Integer.class) {
                    values[i] = Integer.parseInt((String) values[i]);
                } else if (valueTypes[i] == Float.class) {
                    values[i] = Float.parseFloat((String) values[i]);
                } else {
                    return Result.error(this, "§cInvalid value type at index " + i);
                }
            } catch (NumberFormatException e) {
                return Result.error(this, "§cInvalid value for " + valueTypes[i].getSimpleName() + " at index " + i);
            }
        }

        // create the environment
        Environment environment = mineManager.addEnvironment(mine, type, values);
        if (environment == null) {
            return Result.error(this, "§cFailed to create environment!");
        }

        sender.sendMessage("§aEnvironment created in mine: " + mine.getName());
        return Result.success(this);
    }

    private static final List<String> environmentTypes = Arrays.stream(EnvironmentType.values())
        .map(Enum::name)
        .collect(Collectors.toList());

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
        if (strings.length == 1) {
            return filterStartingWith(strings[0], FlawMines.get().getMineManager(((Player) commandSender).getWorld()).getMineNames());
        } else if (strings.length == 2) {
            return filterStartingWith(strings[1], environmentTypes);
        }
        return super.onTabComplete(commandSender, strings);
    }
}
