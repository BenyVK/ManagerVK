package ir.gharri.managerVK.commands;

import ir.gharri.managerVK.ManagerVK;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class ManagerCommand extends BaseCommand {

    public ManagerCommand(ManagerVK plugin) {
        super(plugin, "manager", "vkmanager");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!allowed(sender, "manager.permission")) {
            msg(sender, "messages.no-permission");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfigVK();
            msg(sender, "manager.messages.reloaded");
            return true;
        }
        msg(sender, "manager.messages.usage", "label", label);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && allowed(sender, "manager.permission")) {
            return filter(List.of("reload"), args[0]);
        }
        return List.of();
    }
}
