package ir.gharri.managerVK;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class Messages {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.builder().character('&').hexColors().build();

    // "prefix" must stay first so tokens inside the prefix are resolved too
    private static final String[] THEME_KEYS = {"prefix", "primary", "secondary", "muted", "highlight", "error"};

    private final ManagerVK plugin;

    public Messages(ManagerVK plugin) {
        this.plugin = plugin;
    }

    /** Reads a message from configvk.yml and applies placeholders and theme tokens. */
    public String raw(String path, String... placeholders) {
        String text = plugin.cfg().getString(path, "");
        if (text == null || text.isEmpty()) {
            return "";
        }
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        for (String key : THEME_KEYS) {
            text = text.replace("{" + key + "}", plugin.cfg().getString("theme." + key, ""));
        }
        return text;
    }

    public Component component(String path, String... placeholders) {
        return SERIALIZER.deserialize(raw(path, placeholders));
    }

    /** Sends the message; an empty message in the config sends nothing. */
    public void send(CommandSender to, String path, String... placeholders) {
        String raw = raw(path, placeholders);
        if (raw.isEmpty()) {
            return;
        }
        to.sendMessage(SERIALIZER.deserialize(raw));
    }
}
