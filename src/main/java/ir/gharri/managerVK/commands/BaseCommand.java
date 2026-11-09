package ir.gharri.managerVK.commands;

import ir.gharri.managerVK.ManagerVK;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** Base class: command name, aliases, description and permission all come from configvk.yml. */
public abstract class BaseCommand extends Command {

    protected final ManagerVK plugin;

    protected BaseCommand(ManagerVK plugin, String section, String defaultName) {
        super(plugin.cfg().getString(section + ".command", defaultName),
                plugin.cfg().getString(section + ".description", ""),
                "/" + plugin.cfg().getString(section + ".command", defaultName),
                new ArrayList<>(plugin.cfg().getStringList(section + ".aliases")));
        this.plugin = plugin;
        String node = plugin.cfg().getString(section + ".permission", "");
        if (!node.isBlank()) {
            setPermission(node);
        }
    }

    protected void msg(CommandSender to, String path, String... placeholders) {
        plugin.messages().send(to, path, placeholders);
    }

    /** True when the config path holds no permission or the sender has it. */
    protected boolean allowed(CommandSender sender, String permissionPath) {
        String node = plugin.cfg().getString(permissionPath, "");
        return node.isBlank() || sender.hasPermission(node);
    }

    protected List<String> filter(Collection<String> options, String typed) {
        String lower = typed.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(option);
            }
        }
        return out;
    }

    protected List<String> playerNames(String typed) {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return filter(names, typed);
    }
}
