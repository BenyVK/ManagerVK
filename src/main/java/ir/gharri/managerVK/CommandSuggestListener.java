package ir.gharri.managerVK;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Locale;

/**
 * Bukkit registers every command twice: "rank" and "managervk:rank". The client's tab completer lists both,
 * which clutters chat. This removes our own "managervk:..." copies from the list sent to players, so only
 * "rank" is suggested. Typing "/managervk:rank" by hand still works.
 */
public final class CommandSuggestListener implements Listener {

    private final String prefix;

    public CommandSuggestListener(String pluginName) {
        this.prefix = pluginName.toLowerCase(Locale.ROOT) + ":";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent e) {
        e.getCommands().removeIf(command -> command.toLowerCase(Locale.ROOT).startsWith(prefix));
    }
}
