package dk.martinersej.plugin.command.subcommands;

import dk.martinersej.plugin.FlawMines;
import dk.martinersej.plugin.MineManager;
import dk.martinersej.plugin.mine.Mine;
import dk.martinersej.plugin.mine.MineBlock;
import dk.martinersej.plugin.mine.environment.Environment;
import dk.martinersej.plugin.mine.environment.EnvironmentType;
import dk.martinersej.plugin.utils.command.CommandResult;
import dk.martinersej.plugin.utils.command.Result;
import dk.martinersej.plugin.utils.command.SubCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

public class EnvironmentCreateCommand extends SubCommand {

    public EnvironmentCreateCommand() {
        super(new String[] {"envcreate", "ecreate"}, "Create a new environment in a mine",  "envcreate <mine> <type> <value(s)>", "flawmines.env.create");
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
        Environment environment = mineManager.createEnvironment(mine, type, values);
        if (environment == null) {
            return Result.error(this, "§cFailed to create environment!");
        }

        return Result.success(this);
    }
}
