package ir.gharri.managerVK.commands;

import ir.gharri.managerVK.ManagerVK;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** /survive - lobby only. Sends the player to the survival world after a still-standing countdown. */
public final class SurviveCommand extends BaseCommand {

    private static final long CHECK_PERIOD_TICKS = 5L;

    private final Map<UUID, BukkitTask> pending = new HashMap<>();

    public SurviveCommand(ManagerVK plugin) {
        super(plugin, "survive", "survive");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            msg(sender, "messages.player-only");
            return true;
        }
        if (!allowed(player, "survive.permission")) {
            msg(player, "messages.no-permission");
            return true;
        }

        String currentWorld = player.getWorld().getName();
        if (!isWorldAllowed(currentWorld)) {
            msg(player, "survive.messages.restricted-world", "world", currentWorld);
            return true;
        }

        if (pending.containsKey(player.getUniqueId())) {
            msg(player, "survive.messages.already-teleporting");
            return true;
        }

        Location destination = plugin.surviveTracker().destination(player);
        if (destination == null) {
            msg(player, "survive.messages.not-configured");
            return true;
        }

        int delay = plugin.cfg().getInt("survive.teleport.delay", 3);
        if (delay <= 0) {
            teleport(player, destination);
        } else {
            startCountdown(player, destination, delay,
                    plugin.cfg().getBoolean("survive.teleport.cancel-on-move", true));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    /** Unlike /lobby, an empty list here blocks everything: /survive is meant for specific worlds only. */
    private boolean isWorldAllowed(String worldName) {
        for (String world : plugin.cfg().getStringList("survive.allowed-worlds")) {
            if (world.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    private void startCountdown(Player player, Location destination, int delaySeconds, boolean cancelOnMove) {
        final UUID id = player.getUniqueId();
        final Location origin = player.getLocation().clone();
        final int totalTicks = delaySeconds * 20;

        BukkitTask task = new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    pending.remove(id);
                    cancel();
                    return;
                }
                if (cancelOnMove && hasMoved(origin, player.getLocation())) {
                    pending.remove(id);
                    cancel();
                    player.clearTitle();
                    msg(player, "survive.messages.cancelled");
                    return;
                }
                if (elapsed >= totalTicks) {
                    pending.remove(id);
                    cancel();
                    player.clearTitle();
                    teleport(player, destination);
                    return;
                }
                if (elapsed % 20 == 0) {
                    int secondsLeft = (totalTicks - elapsed) / 20;
                    showCountdown(player, secondsLeft);
                }
                elapsed += (int) CHECK_PERIOD_TICKS;
            }
        }.runTaskTimer(plugin, 0L, CHECK_PERIOD_TICKS);

        pending.put(id, task);
    }

    /** Big number in the middle of the screen with the "don't move" hint underneath. */
    private void showCountdown(Player player, int secondsLeft) {
        String seconds = String.valueOf(secondsLeft);
        Title title = Title.title(
                plugin.messages().component("survive.messages.title", "seconds", seconds),
                plugin.messages().component("survive.messages.subtitle", "seconds", seconds),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ZERO)
        );
        player.showTitle(title);
    }

    private boolean hasMoved(Location from, Location to) {
        return from.getWorld() != to.getWorld()
                || from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }

    private void teleport(Player player, Location destination) {
        player.teleportAsync(destination).thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                msg(player, "survive.messages.success");
            }
        });
    }
}
