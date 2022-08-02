package dev.justjustin.pixelmotd;

import dev.justjustin.pixelmotd.commands.MainCommand;
import dev.justjustin.pixelmotd.exception.NotFoundLanguageException;
import dev.justjustin.pixelmotd.metrics.MetricsHandler;
import dev.justjustin.pixelmotd.players.PlayerHandler;
import dev.justjustin.pixelmotd.servers.ServerHandler;
import dev.justjustin.pixelmotd.storage.MotdStorage;
import dev.justjustin.pixelmotd.utils.FileUtilities;
import dev.justjustin.pixelmotd.utils.Updater;
import dev.mruniverse.slimelib.SlimeFiles;
import dev.mruniverse.slimelib.SlimePlatform;
import dev.mruniverse.slimelib.SlimePlugin;
import dev.mruniverse.slimelib.SlimePluginInformation;
import dev.mruniverse.slimelib.file.configuration.ConfigurationHandler;
import dev.mruniverse.slimelib.file.configuration.ConfigurationProvider;
import dev.mruniverse.slimelib.file.input.InputManager;
import dev.mruniverse.slimelib.loader.BaseSlimeLoader;
import dev.mruniverse.slimelib.loader.DefaultSlimeLoader;
import dev.mruniverse.slimelib.logs.SlimeLogger;
import dev.mruniverse.slimelib.logs.SlimeLogs;

import java.io.File;

@SuppressWarnings("unused")
public class PixelMOTD<T> implements SlimePlugin<T> {

    private final SlimePluginInformation information;

    private final ListenerManager listenerManager;

    private final BaseSlimeLoader<T> slimeLoader;

    private final PlayerHandler playerHandler;

    private final ServerHandler serverHandler;

    private ConfigurationHandler messages;

    private final MotdStorage motdStorage;

    private final SlimePlatform platform;

    private final SlimeLogs logs;

    private final File folder;

    private final File lang;

    private final T plugin;

    public PixelMOTD(SlimePlatform platform, T plugin, File dataFolder) {

        this.folder        = dataFolder;
        this.platform      = platform;
        this.plugin        = plugin;

        this.playerHandler = PlayerHandler.fromPlatform(platform, plugin);

        this.serverHandler = ServerHandler.fromPlatform(platform, plugin);

        this.information   = new SlimePluginInformation(platform, plugin);

        this.logs          = SlimeLogger.createLogs(platform, this);

        SlimeLogger logger = new SlimeLogger();

        logger.setPluginName("PixelMOTD");
        logger.setHidePackage("dev.mruniverse.pixelmotd.");
        logger.setContainIdentifier("mruniverse");
        logger.getProperties().getPrefixes().changeMainText("&9PixelMOTD");

        this.logs.setSlimeLogger(logger);

        this.slimeLoader   = new DefaultSlimeLoader<>(
                this,
                InputManager.getAutomatically()
        );

        getLoader().setFiles(SlimeFile.class);

        getLoader().init();

        getLoader().getCommands().register(new MainCommand<>(this));

        listenerManager = ListenerManager.createNewInstance(
                platform,
                this,
                logs
        );

        if (platform != SlimePlatform.VELOCITY && platform != SlimePlatform.SPONGE) {
            listenerManager.register();
        }

        if (getConfigurationHandler(SlimeFile.SETTINGS).getStatus("settings.update-check", true)) {
            if (getConfigurationHandler(SlimeFile.SETTINGS).getStatus("settings.auto-download-updates", true)) {
                new Updater(logs, information.getVersion(), 37177, getDataFolder(), Updater.UpdateType.CHECK_DOWNLOAD);
            } else {
                new Updater(logs, information.getVersion(), 37177, getDataFolder(), Updater.UpdateType.VERSION_CHECK);
            }

        }

        motdStorage = new MotdStorage(getLoader().getFiles());

        lang = new File(dataFolder, "lang");

        if (!lang.exists()) {
            loadDefaults();
        }

        String code = getConfigurationHandler(SlimeFile.SETTINGS).getString("settings.language", "en");

        File langFile = new File(
                lang,
                "messages_" + code + ".yml"
        );

        if (langFile.exists()) {
            ConfigurationProvider provider = getServerType().getProvider().getNewInstance();

            messages = provider.create(
                    logs,
                    langFile
            );

            logs.info("Messages are loaded from Lang files successfully.");
        } else {
            logs.error("Can't load messages correctly, debug will be showed after this message:");
            logs.debug("Language file of messages: " + langFile.getAbsolutePath());
            logs.debug("Language name file of messages: " + langFile.getName());

        }

        MetricsHandler.fromPlatform(
                platform,
                plugin
        ).initialize();
    }

    private void loadDefaults() {
        loadMessageFile("en");
        loadMessageFile("de");
        loadMessageFile("es");
        loadMessageFile("jp");
        loadMessageFile("pl");
        loadMessageFile("zh-CN");
        loadMessageFile("zh-TW");
    }

    private void loadMessageFile(String file) {
        FileUtilities.load(
                logs,
                lang,
                "lang/messages_" + file + ".yml",
                "/lang/messages_" +  file  +  ".yml"
        );
    }

    public ConfigurationHandler getMessages() {
        if (messages == null) {
            exception();
        }
        return messages;
    }

    private void exception() {
        new NotFoundLanguageException("The current language in the settings file doesn't exists, probably you will see errors in console").printStackTrace();
    }

    public ConfigurationHandler getConfigurationHandler(SlimeFiles file) {
        return getLoader().getFiles().getConfigurationHandler(file);
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public PlayerHandler getPlayerHandler() {
        return playerHandler;
    }

    public ServerHandler getServerHandler() {
        return serverHandler;
    }

    @Override
    public SlimePluginInformation getPluginInformation() {
        return information;
    }

    @Override
    public SlimePlatform getServerType() {
        return platform;
    }

    @Override
    public SlimeLogs getLogs() {
        return logs;
    }

    @Override
    public BaseSlimeLoader<T> getLoader() {
        return slimeLoader;
    }

    @Override
    public T getPlugin() {
        return plugin;
    }

    @Override
    public void reload() {
        slimeLoader.reload();

        listenerManager.update();

        motdStorage.update(
                slimeLoader.getFiles()
        );
    }

    @Override
    public File getDataFolder() {
        return folder;
    }
}
