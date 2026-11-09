package ir.gharri.managerVK.commands;

import ir.gharri.managerVK.ManagerVK;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EffectCommand extends BaseCommand {

    public EffectCommand(ManagerVK plugin) {
        super(plugin, "effect", "effect");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!allowed(sender, "effect.permission")) {
            msg(sender, "messages.no-permission");
            return true;
        }
        if (args.length == 0) {
            msg(sender, "effect.messages.usage", "label", label);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> give(sender, label, args);
            case "clear" -> clear(sender, args);
            default -> msg(sender, "effect.messages.usage", "label", label);
        }
        return true;
    }

    private void give(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            msg(sender, "effect.messages.usage", "label", label);
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            msg(sender, "messages.player-not-found", "player", args[1]);
            return;
        }
        PotionEffectType type = lookup(args[2]);
        if (type == null) {
            msg(sender, "effect.messages.invalid-effect", "effect", args[2]);
            return;
        }

        int seconds = parseInt(args, 3, plugin.cfg().getInt("effect.default-duration", 30));
        int amplifier = parseInt(args, 4, plugin.cfg().getInt("effect.default-amplifier", 0));
        if (seconds <= 0 || amplifier < 0 || amplifier > 255) {
            msg(sender, "effect.messages.invalid-number");
            return;
        }
        int max = Math.min(plugin.cfg().getInt("effect.max-duration", 1_000_000), 100_000_000);
        seconds = Math.min(seconds, max);

        boolean particles = !plugin.cfg().getBoolean("effect.hide-particles", false);
        target.addPotionEffect(new PotionEffect(type, seconds * 20, amplifier, false, particles));

        String name = displayName(type);
        String level = String.valueOf(amplifier + 1);
        String secs = String.valueOf(seconds);
        msg(sender, "effect.messages.given", "effect", name, "level", level, "player", target.getName(), "seconds", secs);
        if (!target.equals(sender)) {
            msg(target, "effect.messages.given-target", "effect", name, "level", level, "seconds", secs);
        }
    }

    private void clear(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                msg(sender, "messages.player-not-found", "player", args[1]);
                return;
            }
        } else if (sender instanceof Player self) {
            target = self;
        } else {
            msg(sender, "messages.player-only");
            return;
        }

        if (args.length >= 3) {
            PotionEffectType type = lookup(args[2]);
            if (type == null) {
                msg(sender, "effect.messages.invalid-effect", "effect", args[2]);
                return;
            }
            if (!target.hasPotionEffect(type)) {
                msg(sender, "effect.messages.no-effects", "player", target.getName());
                return;
            }
            target.removePotionEffect(type);
            msg(sender, "effect.messages.cleared-one", "effect", displayName(type), "player", target.getName());
        } else {
            if (target.getActivePotionEffects().isEmpty()) {
                msg(sender, "effect.messages.no-effects", "player", target.getName());
                return;
            }
            for (PotionEffect active : new ArrayList<>(target.getActivePotionEffects())) {
                target.removePotionEffect(active.getType());
            }
            msg(sender, "effect.messages.cleared-all", "player", target.getName());
        }

        if (!target.equals(sender)) {
            msg(target, "effect.messages.cleared-target", "sender", sender.getName());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!allowed(sender, "effect.permission")) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(List.of("give", "clear"), args[0]);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && (sub.equals("give") || sub.equals("clear"))) {
            return playerNames(args[1]);
        }
        if (args.length == 3 && (sub.equals("give") || sub.equals("clear"))) {
            return filter(effectKeys(), args[2]);
        }
        if (args.length == 4 && sub.equals("give")) {
            return filter(List.of(String.valueOf(plugin.cfg().getInt("effect.default-duration", 30))), args[3]);
        }
        if (args.length == 5 && sub.equals("give")) {
            return filter(List.of("0", "1", "2"), args[4]);
        }
        return List.of();
    }

    private int parseInt(String[] args, int index, int fallback) {
        if (args.length <= index) {
            return fallback;
        }
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    @SuppressWarnings("deprecation")
    private PotionEffectType lookup(String input) {
        String key = input.toLowerCase(Locale.ROOT);
        if (key.startsWith("minecraft:")) {
            key = key.substring("minecraft:".length());
        }
        try {
            return PotionEffectType.getByKey(NamespacedKey.minecraft(key));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private List<String> effectKeys() {
        List<String> keys = new ArrayList<>();
        for (PotionEffectType type : PotionEffectType.values()) {
            keys.add(type.getKey().getKey());
        }
        return keys;
    }

    private String displayName(PotionEffectType type) {
        String[] words = type.getKey().getKey().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}
