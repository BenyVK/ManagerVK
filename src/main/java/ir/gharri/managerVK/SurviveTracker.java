package ir.gharri.managerVK;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.io.IOException;

/**
 * Remembers the last position of every player inside the survival world
 * (stored in survive-data.yml) so /survive can send them back to the same spot.
 */
public final class SurviveTracker implements Listener {

    private final ManagerVK plugin;
    private final File file;
    private final YamlConfiguration data;

    public SurviveTracker(ManagerVK plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "survive-data.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    private String targetWorldName() {
        return plugin.cfg().getString("survive.target-world", "world");
    }

    private boolean isTarget(World world) {
        return world != null && world.getName().equalsIgnoreCase(targetWorldName());
    }

    /**
     * Where the player should land: the saved position if they have been in the
     * survival world before, otherwise the configured first-time position.
     * Returns null when the survival world is not loaded.
     */
    public Location destination(Player player) {
        World world = Bukkit.getWorld(targetWorldName());
        if (world == null) {
            return null;
        }
        ConfigurationSection saved = data.getConfigurationSection("players." + player.getUniqueId());
        if (saved != null) {
            return read(world, saved);
        }
        ConfigurationSection first = plugin.cfg().getConfigurationSection("survive.first-time");
        if (first != null) {
            return read(world, first);
        }
        return new Location(world, 0, 0, 0, 0f, 0f);
    }

    private Location read(World world, ConfigurationSection s) {
        return new Location(
                world,
                s.getDouble("x"),
                s.getDouble("y"),
                s.getDouble("z"),
                (float) s.getDouble("yaw"),
                (float) s.getDouble("pitch")
        );
    }

    private void remember(Player player, Location loc) {
        String base = "players." + player.getUniqueId() + ".";
        data.set(base + "x", loc.getX());
        data.set(base + "y", loc.getY());
        data.set(base + "z", loc.getZ());
        data.set(base + "yaw", (double) loc.getYaw());
        data.set(base + "pitch", (double) loc.getPitch());
        save();
    }

    /** Called on shutdown so players still standing in the survival world are not lost. */
    public void saveOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isTarget(player.getWorld())) {
                String base = "players." + player.getUniqueId() + ".";
                Location loc = player.getLocation();
                data.set(base + "x", loc.getX());
                data.set(base + "y", loc.getY());
                data.set(base + "z", loc.getZ());
                data.set(base + "yaw", (double) loc.getYaw());
                data.set(base + "pitch", (double) loc.getPitch());
            }
        }
        save();
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save survive-data.yml: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Events
    // ------------------------------------------------------------------

    /** Leaving the survival world by any teleport (/lobby, portals, other plugins). */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || from.getWorld() == null || to.getWorld() == null) {
            return;
        }
        if (from.getWorld().equals(to.getWorld())) {
            return;
        }
        if (isTarget(from.getWorld())) {
            remember(event.getPlayer(), from);
        }
    }

    /** Logging out while inside the survival world. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isTarget(player.getWorld())) {
            remember(player, player.getLocation());
        }
    }
}
