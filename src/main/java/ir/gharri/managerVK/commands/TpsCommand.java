package ir.gharri.managerVK.commands;

import ir.gharri.managerVK.ManagerVK;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;

public final class TpsCommand extends BaseCommand {

    public TpsCommand(ManagerVK plugin) {
        super(plugin, "tps", "tps");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!allowed(sender, "tps.permission")) {
            msg(sender, "messages.no-permission");
            return true;
        }
        double[] tps = Bukkit.getTPS();
        double mspt = Bukkit.getAverageTickTime();

        msg(sender, "tps.messages.header");
        msg(sender, "tps.messages.tps-line",
                "tps1", format(tps[0]),
                "tps5", format(tps[1]),
                "tps15", format(tps[2]));
        msg(sender, "tps.messages.mspt-line", "mspt", String.format(Locale.ROOT, "%.2f", mspt));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    private String format(double rawTps) {
        double tps = Math.min(20.0, rawTps);
        double good = plugin.cfg().getDouble("tps.thresholds.good", 18.0);
        double medium = plugin.cfg().getDouble("tps.thresholds.medium", 15.0);

        String color;
        if (tps >= good) {
            color = plugin.cfg().getString("tps.colors.good", "&a");
        } else if (tps >= medium) {
            color = plugin.cfg().getString("tps.colors.medium", "&e");
        } else {
            color = plugin.cfg().getString("tps.colors.bad", "&c");
        }
        return color + String.format(Locale.ROOT, "%.2f", tps);
    }
}
