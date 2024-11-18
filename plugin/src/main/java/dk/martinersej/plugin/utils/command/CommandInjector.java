package dk.martinersej.plugin.utils.command;

import dk.martinersej.plugin.FlawMines;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;

public class CommandInjector {

    private SimpleCommandMap scm;

    public CommandInjector() {
        setupSimpleCommandMap();
    }

    private void setupSimpleCommandMap() {
        SimplePluginManager spm = (SimplePluginManager) FlawMines.get().getServer().getPluginManager();
        Field field = null;
        try {
            field = SimplePluginManager.class.getDeclaredField("commandMap");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (field == null) { // Should never happen
            throw new IllegalStateException("Could not find field 'commandMap' in SimplePluginManager!");
        }
        field.setAccessible(true);
        try {
            scm = (SimpleCommandMap) field.get(spm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableCommand(Command command, Plugin owner) {
        scm.register(owner.getName(), command);
    }

    public void disableCommand(Command command) {
        scm.getCommand(command.getName()).unregister(scm);
    }
}