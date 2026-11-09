package ir.gharri.managerVK.commands;

import ir.gharri.managerVK.ManagerVK;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class GamemodeCommand extends BaseCommand {

    public GamemodeCommand(ManagerVK plugin) {
        super(plugin, "gamemode", "gamemode");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!allowed(sender, "gamemode.permission")) {
            msg(sender, "messages.no-permission");
            return true;
        }
        if (args.length == 0) {
            msg(sender, "gamemode.messages.usage", "label", label);
            return true;
        }

        GameMode mode = parseMode(args[0]);
        if (mode == null) {
            msg(sender, "gamemode.messages.invalid-mode", "input", args[0]);
            return true;
        }

        Player target;
        if (args.length >= 2) {
            if (!allowed(sender, "gamemode.permission-others")) {
                msg(sender, "messages.no-permission");
                return true;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                msg(sender, "messages.player-not-found", "player", args[1]);
                return true;
            }
        } else if (sender instanceof Player self) {
            target = self;
        } else {
            msg(sender, "messages.player-only");
            return true;
        }

        target.setGameMode(mode);
        String name = modeName(mode);

        if (target.equals(sender)) {
            msg(sender, "gamemode.messages.changed-self", "mode", name);
        } else {
            msg(sender, "gamemode.messages.changed-other", "player", target.getName(), "mode", name);
            msg(target, "gamemode.messages.changed-by-other", "mode", name, "sender", sender.getName());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!allowed(sender, "gamemode.permission")) {
            return List.of();
        }
        if (args.length == 1) {
            ConfigurationSection shortcuts = plugin.cfg().getConfigurationSection("gamemode.shortcuts");
            if (shortcuts == null) {
                return List.of();
            }
            return filter(shortcuts.getKeys(false), args[0]);
        }
        if (args.length == 2 && allowed(sender, "gamemode.permission-others")) {
            return playerNames(args[1]);
        }
        return List.of();
    }

    private GameMode parseMode(String input) {
        ConfigurationSection shortcuts = plugin.cfg().getConfigurationSection("gamemode.shortcuts");
        if (shortcuts != null) {
            for (String key : shortcuts.getKeys(false)) {
                if (key.equalsIgnoreCase(input)) {
                    return fromName(shortcuts.getString(key));
                }
            }
        }
        // Also accept full names like "creative"
        return fromName(input);
    }

    private GameMode fromName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return GameMode.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String modeName(GameMode mode) {
        String lower = mode.name().toLowerCase(Locale.ROOT);
        String fallback = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        return plugin.cfg().getString("gamemode.names." + lower, fallback);
    }
}
