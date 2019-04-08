package me.fixeddev.nametag;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.UUID;

public class ProtocolLibNametagApi implements NametagApi {

    private ProtocolManager protocolManager;
    private Map<UUID, String> nametagMap;

    public ProtocolLibNametagApi(JavaPlugin plugin) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        nametagMap = Maps.newConcurrentMap();

        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                StructureModifier<WrappedGameProfile> profiles = event.getPacket().getGameProfiles();

                Player toDisplay = (Player) event.getPacket().getEntityModifier(event).read(0);

                WrappedGameProfile original = profiles.read(0);
                WrappedGameProfile result;

                String nametag = getPlayerNametag(toDisplay);

                String prefix = "";
                String name = "";
                String suffix = "";

                if (nametag.length() < 16) {
                    name = nametag;
                } else if (nametag.length() > 16 && nametag.length() < 32) {
                    prefix = nametag.substring(0, 16);
                    name = nametag.substring(16);
                } else {
                    prefix = nametag.substring(0, 16);
                    name = nametag.substring(16, 32);
                    suffix = nametag.substring(32, 48);
                }

                if (!prefix.isEmpty() || !suffix.isEmpty()) {
                    setNametagPrefixAndSuffix(toDisplay, prefix, suffix);
                }

                result = original.withName(name);
                profiles.write(0, result);
            }
        });
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
    public void setNametag(Player player, String nametag) {
        nametagMap.put(player.getUniqueId(), nametag);

        Bukkit.getOnlinePlayers().forEach(viewer -> {
            boolean canSee = viewer.canSee(player);

            viewer.hidePlayer(player);

            if (canSee) {
                viewer.showPlayer(player);
            }
        });
    }

    @Override
    public String getPlayerNametag(Player player) {
        return nametagMap.getOrDefault(player.getUniqueId(), player.getName());
    }
}
