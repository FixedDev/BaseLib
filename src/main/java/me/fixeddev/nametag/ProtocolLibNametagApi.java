package me.fixeddev.nametag;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ProtocolLibNametagApi implements NametagApi {

    private static Method PLAYER_GET_HANDLE_METHOD;
    private static Method HANDLE_GET_PROFILE_METHOD;
    private static Field PROFILE_NAME_FIELD;

    private ProtocolManager protocolManager;
    private JavaPlugin plugin;

    private Map<UUID, Tag> nametagMap;

    public ProtocolLibNametagApi(JavaPlugin plugin) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.plugin = plugin;

        nametagMap = Maps.newConcurrentMap();

        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.NAMED_ENTITY_SPAWN,
                PacketType.Play.Server.ENTITY_EFFECT,
                PacketType.Play.Server.ENTITY_EQUIPMENT,
                PacketType.Play.Server.ENTITY_METADATA,
                PacketType.Play.Server.UPDATE_ATTRIBUTES,
                PacketType.Play.Server.ATTACH_ENTITY,
                PacketType.Play.Server.BED) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Entity entity = event.getPacket().getEntityModifier(event).read(0);

                if (!(entity instanceof Player)) {
                    return;
                }

                Player toDisplay = (Player) event.getPacket().getEntityModifier(event).read(0);

                Tag nametag = getPlayerNametag(toDisplay);

                if (nametag.hasPrefix() || nametag.hasSuffix()) {
                    setNametagPrefixAndSuffix(toDisplay, nametag.getPrefix(), nametag.getSuffix());
                }

                try {
                    setGameProfileName(toDisplay, nametag.getName());
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
                    plugin.getLogger().log(Level.SEVERE, "An error ocurred while changing player " + toDisplay.getName() + " game profile");
                }
            }
        });
    }

    private void setGameProfileName(Player player, String name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        if (PLAYER_GET_HANDLE_METHOD == null) {
            PLAYER_GET_HANDLE_METHOD = player.getClass().getMethod("getHandle");
        }

        boolean accessible = PLAYER_GET_HANDLE_METHOD.isAccessible();
        PLAYER_GET_HANDLE_METHOD.setAccessible(true);

        Object playerHandle = PLAYER_GET_HANDLE_METHOD.invoke(player);
        PLAYER_GET_HANDLE_METHOD.setAccessible(accessible);

        if (HANDLE_GET_PROFILE_METHOD == null) {
            HANDLE_GET_PROFILE_METHOD = playerHandle.getClass().getMethod("getProfile");
        }
        accessible = HANDLE_GET_PROFILE_METHOD.isAccessible();

        GameProfile playerProfile = (GameProfile) HANDLE_GET_PROFILE_METHOD.invoke(playerHandle);
        HANDLE_GET_PROFILE_METHOD.setAccessible(accessible);

        if (PROFILE_NAME_FIELD == null) {
            PROFILE_NAME_FIELD = playerProfile.getClass().getDeclaredField("name");
        }
        accessible = PROFILE_NAME_FIELD.isAccessible();

        PROFILE_NAME_FIELD.setAccessible(true);
        PROFILE_NAME_FIELD.set(playerProfile, name);

        PROFILE_NAME_FIELD.setAccessible(accessible);
    }

    private void setNametagPrefixAndSuffix(Player player, String prefix, String suffix) {
        Bukkit.getOnlinePlayers().forEach(o -> {
            Scoreboard playerScoreboard = o.getScoreboard();

            Team team = playerScoreboard.getTeam(player.getName());
            if (team == null) {
                team = playerScoreboard.registerNewTeam(player.getName());
            }

            team.setPrefix(prefix);
            team.setSuffix(suffix);
            team.addEntry(player.getName());
            team.setNameTagVisibility(NameTagVisibility.ALWAYS);
        });
    }

    @Override
    public void setNametag(Player player, String prefix, String name, String suffix) {
        Tag tag = new SimpleTag(prefix, name, suffix);

        nametagMap.put(player.getUniqueId(), tag);

        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                Bukkit.getOnlinePlayers().forEach(o -> {
                    boolean canSee = o.canSee(player);

                    o.hidePlayer(player);

                    if (canSee) {
                        o.showPlayer(player);
                    }
                }), 1);

        protocolManager.updateEntity(player, protocolManager.getEntityTrackers(player));
    }

    @NotNull
    @Override
    public Tag getPlayerNametag(Player player) {
        return nametagMap.getOrDefault(player.getUniqueId(), new SimpleTag(player.getName()));
    }
}
