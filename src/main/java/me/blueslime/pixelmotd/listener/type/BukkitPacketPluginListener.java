package me.blueslime.pixelmotd.listener.type;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import me.blueslime.pixelmotd.listener.bukkit.BukkitListener;
import me.blueslime.slimelib.file.configuration.ConfigurationHandler;
import me.blueslime.slimelib.logs.SlimeLogs;
import me.blueslime.pixelmotd.Configuration;
import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.exception.NotFoundLanguageException;
import me.blueslime.pixelmotd.listener.PluginListener;
import me.blueslime.pixelmotd.players.PlayerDatabase;
import me.blueslime.pixelmotd.players.PlayerHandler;
import me.blueslime.pixelmotd.servers.ServerHandler;
import me.blueslime.pixelmotd.utils.ListType;
import me.blueslime.pixelmotd.utils.list.PluginList;
import me.blueslime.pixelmotd.utils.placeholders.PluginPlaceholders;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public abstract class BukkitPacketPluginListener extends PacketAdapter implements PluginListener<JavaPlugin>, Listener {
    private final PixelMOTD<JavaPlugin> plugin;
    private final BukkitListener listener;

    @SuppressWarnings("unchecked")
    public BukkitPacketPluginListener(PixelMOTD<?> plugin, BukkitListener listener, ListenerPriority priority, PacketType... types) {
        super(
                (JavaPlugin) plugin.getPlugin(),
                priority,
                types
        );
        this.listener = listener;
        this.plugin = (PixelMOTD<JavaPlugin>) plugin;

    }

    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    public boolean checkPlayer(ListType listType, String path, String username) {
        if (path.equals("global")) {
            return plugin.getConfiguration(listType.getFile()).getStringList("players.by-name").contains(username);
        }
        return plugin.getConfiguration(listType.getFile()).getStringList(path + ".players.by-name").contains(username);
    }


    public boolean checkUUID(ListType listType, String path, UUID uniqueId) {
        if (path.equals("global")) {
            return plugin.getConfiguration(listType.getFile()).getStringList("players.by-uuid").contains(uniqueId.toString());
        }
        return plugin.getConfiguration(listType.getFile()).getStringList(path + ".players.by-uuid").contains(uniqueId.toString());
    }

    @Override
    public ConfigurationHandler getWhitelist() {
        return plugin.getConfiguration(Configuration.WHITELIST);
    }

    @Override
    public ConfigurationHandler getBlacklist() {
        return plugin.getConfiguration(Configuration.BLACKLIST);
    }

    @Override
    public ConfigurationHandler getSettings() {
        return plugin.getSettings();
    }

    @Override
    public ConfigurationHandler getMessages() throws NotFoundLanguageException {
        return plugin.getMessages();
    }

    @Override
    public ConfigurationHandler getConfiguration(Configuration configuration) {
        return plugin.getConfiguration(configuration);
    }

    @Override
    public PluginPlaceholders getPlaceholders() {
        return plugin.getPlaceholders();
    }

    @Override
    public PlayerHandler getPlayerHandler() {
        return plugin.getPlayerHandler();
    }

    @Override
    public ServerHandler getServerHandler() {
        return plugin.getServerHandler();
    }

    @Override
    public SlimeLogs getLogs() {
        return plugin.getLogs();
    }

    @Override
    public String getName() {
        return listener.toString();
    }

    @Override
    public PluginList getSerializer() {
        return PluginList.fromPlatform(plugin.getServerType());
    }

    @Override
    public PlayerDatabase getPlayerDatabase() {
        return plugin.getLoader().getDatabase();
    }

    public PixelMOTD<JavaPlugin> getBasePlugin() {
        return plugin;
    }
}
