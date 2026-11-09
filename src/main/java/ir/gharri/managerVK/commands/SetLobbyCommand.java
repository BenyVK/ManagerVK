package ir.gharri.managerVK.commands;

import ir.gharri.managerVK.ManagerVK;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class SetLobbyCommand extends BaseCommand {

    public SetLobbyCommand(ManagerVK plugin) {
        super(plugin, "lobby.set", "setlobby");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            msg(sender, "messages.player-only");
            return true;
        }
        if (!allowed(player, "lobby.set.permission")) {
            msg(player, "messages.no-permission");
            return true;
        }

        Location loc = player.getLocation();
        double x = round(loc.getX());
        double y = round(loc.getY());
        double z = round(loc.getZ());

        plugin.cfg().set("lobby.destination.world", loc.getWorld().getName());
        plugin.cfg().set("lobby.destination.x", x);
        plugin.cfg().set("lobby.destination.y", y);
        plugin.cfg().set("lobby.destination.z", z);
        plugin.cfg().set("lobby.destination.yaw", round(loc.getYaw()));
        plugin.cfg().set("lobby.destination.pitch", round(loc.getPitch()));
        plugin.saveConfigVK();

        msg(player, "lobby.messages.set-success",
                "world", loc.getWorld().getName(),
                "x", String.valueOf(x),
                "y", String.valueOf(y),
                "z", String.valueOf(z));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
