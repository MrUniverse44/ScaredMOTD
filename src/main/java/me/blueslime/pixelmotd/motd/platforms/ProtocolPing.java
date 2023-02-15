package me.blueslime.pixelmotd.motd.platforms;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import dev.mruniverse.slimelib.colors.platforms.StringSlimeColor;
import dev.mruniverse.slimelib.file.configuration.ConfigurationHandler;
import dev.mruniverse.slimelib.file.configuration.TextDecoration;
import me.blueslime.pixelmotd.motd.MotdProtocol;
import me.blueslime.pixelmotd.motd.MotdType;
import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.external.iridiumcolorapi.IridiumColorAPI;
import me.blueslime.pixelmotd.motd.builder.PingBuilder;
import me.blueslime.pixelmotd.motd.builder.favicon.FaviconModule;
import me.blueslime.pixelmotd.motd.builder.hover.HoverModule;
import me.blueslime.pixelmotd.utils.MotdPlayers;
import me.blueslime.pixelmotd.utils.PlaceholderParser;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ProtocolPing extends PingBuilder<JavaPlugin, WrappedServerPing.CompressedImage, WrappedServerPing, WrappedGameProfile> {

    private final boolean hasPAPI;

    public ProtocolPing(
            PixelMOTD<JavaPlugin> plugin,
            FaviconModule<JavaPlugin, WrappedServerPing.CompressedImage> builder,
            HoverModule<WrappedGameProfile> hoverModule
    ) {
        super(plugin, builder, hoverModule);
        hasPAPI = plugin.getPlugin().getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    @Override
    public void execute(MotdType motdType, WrappedServerPing ping, int code, String user) {
        final ConfigurationHandler control = getPlugin().getConfigurationHandler(motdType.getFile());

        String motd = getMotd(motdType);

        if (motd.equals("8293829382382732127413475y42732749832748327472fyfs")) {
            if (isDebug()) {
                getLogs().debug("The plugin don't detect motds for MotdType: " + motdType);
            }
            return;
        }

        String path = motdType + "." + motd + ".";

        String line1, line2, completed;

        int online, max;

        if (isIconSystem()) {
            String iconName = control.getString(
                    path + "icons.icon", ""
            );

            if (!iconName.equalsIgnoreCase("") && !iconName.equalsIgnoreCase("disabled")) {
                WrappedServerPing.CompressedImage img = getFavicon().getFavicon(
                        iconName
                );
                if (img != null) {
                    ping.setFavicon(img);
                }
            }
        }

        if (control.getStatus(path + "players.online.toggle", false)) {
            online = MotdPlayers.getModeFromText(
                    control,
                    control.getString(path + "players.online.type", "add"),
                    getPlugin().getPlayerHandler().getPlayersSize(),
                    path + "players.online."
            );
        } else {
            online = ping.getPlayersOnline();
        }
        if (control.getStatus(path + "players.max.toggle", false)) {
            String mode = control.getString(path + "players.max.type", "add").toLowerCase();
            if (mode.contains("equal")) {
                max = MotdPlayers.getModeFromText(
                        control,
                        mode,
                        ping.getPlayersMaximum(),
                        path + "players.max."
                );
            } else {
                max = MotdPlayers.getModeFromText(
                        control,
                        mode,
                        online,
                        path + "players.max."
                );
            }
        } else {
            max = ping.getPlayersMaximum();
        }

        if (control.getStatus(path + "hover.toggle", false)) {
            ping.setPlayers(
                    getHoverModule().generate(
                            control,
                            path,
                            user,
                            online,
                            max
                    )
            );
        }

        if (control.getStatus(path + "protocol.toggle", true)) {
            MotdProtocol protocol = MotdProtocol.getFromText(
                    control.getString(path + "protocol.modifier", "ALWAYS_POSITIVE"),
                    code
            );

            if (protocol != MotdProtocol.ALWAYS_NEGATIVE) {
                ping.setVersionProtocol(protocol.getCode());
            }

            ping.setVersionName(
                    ChatColor.translateAlternateColorCodes(
                            '&',
                            getExtras().replace(
                                    control.getString(path + "protocol.message", "PixelMOTD System"),
                                    online,
                                    max,
                                    user
                            )
                    )
            );
        }

        if (!motdType.isHexMotd()) {
            line1 = control.getString(TextDecoration.LEGACY, path + "line1", "");
            line2 = control.getString(TextDecoration.LEGACY, path + "line2", "");

            if (hasPAPI) {
                line1 = PlaceholderParser.parse(getPlugin().getLogs(), user, line1);
                line2 = PlaceholderParser.parse(getPlugin().getLogs(), user, line2);
            }

            completed = getExtras().replace(
                    line1,
                    online,
                    max,
                    user
            ) + "\n" + getExtras().replace(
                    line2,
                    online,
                    max,
                    user
            );

        } else {
            line1 = control.getString(path + "line1", "");
            line2 = control.getString(path + "line2", "");

            if (hasPAPI) {
                line1 = PlaceholderParser.parse(getPlugin().getLogs(), user, line1);
                line2 = PlaceholderParser.parse(getPlugin().getLogs(), user, line2);
            }

            completed = getExtras().replace(
                    line1,
                    online,
                    max,
                    user
            ) + "\n" + getExtras().replace(
                    line2,
                    online,
                    max,
                    user
            );

            if (completed.contains("<GRADIENT") || completed.contains("<RAINBOW") || completed.contains("<SOLID:")) {

                completed = IridiumColorAPI.process(completed);

            } else {
                completed = new StringSlimeColor(
                        completed,
                        true
                ).build();
            }

            completed = ChatColor.translateAlternateColorCodes('&', completed);
        }

        ping.setMotD(completed);
        ping.setPlayersOnline(online);
        ping.setPlayersMaximum(max);
    }
}
