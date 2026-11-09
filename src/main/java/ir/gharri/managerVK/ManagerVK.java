package ir.gharri.managerVK;

import ir.gharri.managerVK.commands.EffectCommand;
import ir.gharri.managerVK.commands.GamemodeCommand;
import ir.gharri.managerVK.commands.LobbyCommand;
import ir.gharri.managerVK.commands.ManagerCommand;
import ir.gharri.managerVK.commands.SetLobbyCommand;
import ir.gharri.managerVK.commands.TpsCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ManagerVK extends JavaPlugin {

    private static final String CONFIG_NAME = "configvk.yml";

    private File configFile;
    private FileConfiguration config;
    private Messages messages;

    private final Map<String, Command> installed = new LinkedHashMap<>();
    private final Map<String, Command> displaced = new HashMap<>();
    private final List<Permission> registeredPermissions = new ArrayList<>();

    @Override
    public void onEnable() {
        configFile = new File(getDataFolder(), CONFIG_NAME);
        if (!configFile.exists()) {
            saveResource(CONFIG_NAME, false);
        }
        messages = new Messages(this);
        reloadConfigVK();
        registerPermissions();
        registerCommands();
    }

    @Override
    public void onDisable() {
        try {
            Map<String, Command> known = getServer().getCommandMap().getKnownCommands();
            String prefix = getName().toLowerCase(Locale.ROOT);
            for (Map.Entry<String, Command> entry : installed.entrySet()) {
                String label = entry.getKey();
                known.remove(prefix + ":" + label, entry.getValue());
                if (known.get(label) == entry.getValue()) {
                    Command old = displaced.get(label);
                    if (old != null) {
                        known.put(label, old);
                    } else {
                        known.remove(label);
                    }
                }
            }
        } catch (UnsupportedOperationException ignored) {
            // command map is read-only on this server build
        }
        installed.clear();
        displaced.clear();

        PluginManager pm = getServer().getPluginManager();
        for (Permission permission : registeredPermissions) {
            pm.removePermission(permission);
        }
        registeredPermissions.clear();
    }

    // ------------------------------------------------------------------
    // Config
    // ------------------------------------------------------------------

    public void reloadConfigVK() {
        config = YamlConfiguration.loadConfiguration(configFile);
        try (InputStream in = getResource(CONFIG_NAME)) {
            if (in != null) {
                // Missing keys fall back to the defaults bundled in the jar
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
            getLogger().warning("Could not load bundled defaults for " + CONFIG_NAME + ": " + e.getMessage());
        }
    }

    public boolean saveConfigVK() {
        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            getLogger().severe("Could not save " + CONFIG_NAME + ": " + e.getMessage());
            return false;
        }
    }

    public FileConfiguration cfg() {
        return config;
    }

    public Messages messages() {
        return messages;
    }

    /** Lobby destination from configvk.yml, or null when the world is missing. */
    public Location lobbyLocation() {
        ConfigurationSection s = config.getConfigurationSection("lobby.destination");
        if (s == null) {
            return null;
        }
        World world = Bukkit.getWorld(s.getString("world", ""));
        if (world == null) {
            return null;
        }
        return new Location(
                world,
                s.getDouble("x"),
                s.getDouble("y"),
                s.getDouble("z"),
                (float) s.getDouble("yaw"),
                (float) s.getDouble("pitch")
        );
    }

    // ------------------------------------------------------------------
    // Permissions
    // ------------------------------------------------------------------

    private void registerPermissions() {
        perm("manager.permission", null, "op", "Use /vkmanager");
        perm("gamemode.permission", "gamemode.permission-default", "op", "Change your game mode");
        perm("gamemode.permission-others", null, "op", "Change other players' game mode");
        perm("tps.permission", "tps.permission-default", "true", "Use /tps");
        perm("effect.permission", "effect.permission-default", "op", "Use /effect");
        perm("lobby.permission", "lobby.permission-default", "true", "Use /lobby");
        perm("lobby.teleport.bypass-permission", null, "op", "Skip the /lobby delay");
        perm("lobby.set.permission", null, "op", "Use /setlobby");
    }

    private void perm(String nodePath, String defaultPath, String fallbackDefault, String description) {
        String node = config.getString(nodePath, "");
        if (node.isBlank()) {
            return;
        }
        String def = defaultPath == null ? fallbackDefault : config.getString(defaultPath, fallbackDefault);
        PermissionDefault permissionDefault = PermissionDefault.getByName(def);
        if (permissionDefault == null) {
            permissionDefault = PermissionDefault.OP;
        }
        PluginManager pm = getServer().getPluginManager();
        if (pm.getPermission(node) != null) {
            return;
        }
        Permission permission = new Permission(node, description, permissionDefault);
        pm.addPermission(permission);
        registeredPermissions.add(permission);
    }

    // ------------------------------------------------------------------
    // Commands
    // ------------------------------------------------------------------

    private void registerCommands() {
        install(new ManagerCommand(this));
        if (config.getBoolean("gamemode.enabled", true)) {
            install(new GamemodeCommand(this));
        }
        if (config.getBoolean("tps.enabled", true)) {
            install(new TpsCommand(this));
        }
        if (config.getBoolean("effect.enabled", true)) {
            install(new EffectCommand(this));
        }
        if (config.getBoolean("lobby.enabled", true)) {
            install(new LobbyCommand(this));
            if (config.getBoolean("lobby.set.enabled", true)) {
                install(new SetLobbyCommand(this));
            }
        }
    }

    /**
     * Puts the command straight into the server command map so it also replaces
     * vanilla commands such as /gamemode, /tps and /effect.
     */
    private void install(Command command) {
        CommandMap map = getServer().getCommandMap();
        try {
            Map<String, Command> known = map.getKnownCommands();
            String prefix = getName().toLowerCase(Locale.ROOT);

            List<String> labels = new ArrayList<>();
            labels.add(command.getName());
            labels.addAll(command.getAliases());

            for (String raw : labels) {
                String label = raw.toLowerCase(Locale.ROOT).trim();
                if (label.isEmpty()) {
                    continue;
                }
                Command old = known.get(label);
                if (old != null && old != command) {
                    displaced.putIfAbsent(label, old);
                }
                known.put(label, command);
                known.put(prefix + ":" + label, command);
                installed.put(label, command);
            }
            command.register(map);
            getLogger().info("Registered /" + command.getName());
        } catch (UnsupportedOperationException e) {
            getLogger().severe("Could not register /" + command.getName() + ": command map is read-only.");
        }
    }
}
