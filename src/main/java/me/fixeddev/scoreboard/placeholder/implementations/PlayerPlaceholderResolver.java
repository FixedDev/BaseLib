package me.fixeddev.scoreboard.placeholder.implementations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import me.fixeddev.scoreboard.placeholder.PlaceholderResolver;
import org.bukkit.OfflinePlayer;


import java.util.Map;
import java.util.Set;

public class PlayerPlaceholderResolver implements PlaceholderResolver {

    @Override
    public String replacePlaceholders(OfflinePlayer player, String text) {
        for (Map.Entry<String, String> entry : getPlaceholdersValues(player).entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    @Override
    public Set<String> getPlaceholders() {
        return ImmutableSet.of("{player_name}", "{player_displayname}");
    }

    @Override
    public Map<String, String> getPlaceholdersValues(OfflinePlayer player) {
        ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();
        mapBuilder.put("{player_name}", player.getName());

        if (player.isOnline()) {
            mapBuilder.put("{player_displayname}", player.getPlayer().getDisplayName());
        } else {
            mapBuilder.put("{player_displayname}", player.getName());
        }

        return mapBuilder.build();
    }
}
