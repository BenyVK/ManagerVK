package ir.gharri.managerVK.commands;

import ir.gharri.managerVK.ManagerVK;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LobbyCommand extends BaseCommand {

    private static final long CHECK_PERIOD_TICKS = 5L;

    private final Map<UUID, BukkitTask> pending = new HashMap<>();

    public LobbyCommand(ManagerVK plugin) {
        super(plugin, "lobby", "lobby");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            msg(sender, "messages.player-only");
            return true;
        }
        if (!allowed(player, "lobby.permission")) {
            msg(player, "messages.no-permission");
            return true;
        }

        String currentWorld = player.getWorld().getName();
        if (!isWorldAllowed(currentWorld)) {
            msg(player, "lobby.messages.restricted-world", "world", currentWorld);
            return true;
        }

        Location destination = plugin.lobbyLocation();
        if (destination == null) {
            msg(player, "lobby.messages.not-configured");
            return true;
        }

        if (pending.containsKey(player.getUniqueId())) {
            msg(player, "lobby.messages.already-teleporting");
            return true;
        }

        int delay = plugin.cfg().getInt("lobby.teleport.delay", 3);
        String bypass = plugin.cfg().getString("lobby.teleport.bypass-permission", "");
        if (!bypass.isBlank() && player.hasPermission(bypass)) {
            delay = 0;
        }

        msg(player, "lobby.messages.teleporting");
        if (delay <= 0) {
            teleport(player, destination);
        } else {
            startCountdown(player, destination, delay, plugin.cfg().getBoolean("lobby.teleport.cancel-on-move", true));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    /** An empty allowed-worlds list means every world is allowed. */
    private boolean isWorldAllowed(String worldName) {
        List<String> allowed = plugin.cfg().getStringList("lobby.allowed-worlds");
        if (allowed.isEmpty()) {
            return true;
        }
        for (String world : allowed) {
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
                    msg(player, "lobby.messages.cancelled");
                    return;
                }
                if (elapsed >= totalTicks) {
                    pending.remove(id);
                    cancel();
                    teleport(player, destination);
                    return;
                }
                if (elapsed % 20 == 0) {
                    int secondsLeft = (totalTicks - elapsed) / 20;
                    msg(player, "lobby.messages.countdown", "seconds", String.valueOf(secondsLeft));
                }
                elapsed += (int) CHECK_PERIOD_TICKS;
            }
        }.runTaskTimer(plugin, 0L, CHECK_PERIOD_TICKS);

        pending.put(id, task);
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
                msg(player, "lobby.messages.success");
            }
        });
    }
}
