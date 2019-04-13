package me.fixeddev.nametag;

import com.google.common.collect.Maps;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class SimpleNametagApi implements NametagApi {
    private Map<UUID, Tag> nametagMap;

    public SimpleNametagApi() {
        this.nametagMap = Maps.newConcurrentMap();
    }

    @Override
    public void setNametag(Player player, String prefix, String name, String suffix) {
        Tag tag = new SimpleTag(prefix, name, suffix);

        nametagMap.put(player.getUniqueId(), tag);

        player.setPlayerListName(prefix + name + suffix);
    }


    @NotNull
    @Override
    public Tag getPlayerNametag(Player player) {
        return nametagMap.getOrDefault(player.getUniqueId(), new SimpleTag(player.getName()));
    }
}
