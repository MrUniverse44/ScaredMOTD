package me.blueslime.pixelmotd;

import me.blueslime.slimelib.file.configuration.ConfigurationHandler;
import me.blueslime.slimelib.logs.SlimeLogs;
import me.blueslime.pixelmotd.exception.NotFoundLanguageException;

public abstract class PluginModule {
    private final PixelMOTD<?> plugin;

    public PluginModule(PixelMOTD<?> plugin) {
        this.plugin = plugin;
    }

    public PixelMOTD<?> getPlugin() {
        return plugin;
    }

    public ConfigurationHandler getSettings() {
        return plugin.getSettings();
    }

    public ConfigurationHandler getMessages() throws NotFoundLanguageException {
        return plugin.getMessages();
    }

    public SlimeLogs getLogs() {
        return plugin.getLogs();
    }
}
