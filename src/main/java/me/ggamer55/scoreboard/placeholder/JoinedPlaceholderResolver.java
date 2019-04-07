

package me.ggamer55.scoreboard.placeholder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class JoinedPlaceholderResolver implements PlaceholderResolver {
    private Set<PlaceholderResolver> placeholderResolvers;

    public JoinedPlaceholderResolver() {
        placeholderResolvers = new HashSet<>();
    }

    public JoinedPlaceholderResolver(Set<PlaceholderResolver> placeholderResolvers) {
        Preconditions.checkNotNull(placeholderResolvers);
        this.placeholderResolvers = new HashSet<>(placeholderResolvers);
    }

    public void registerPlaceholderReplacer(PlaceholderResolver placeholderResolver) {
        Preconditions.checkArgument(!equals(placeholderResolver));
        Preconditions.checkState(!placeholderResolvers.contains(placeholderResolver), "Tried to register an already registered placeholder replacer!");

        placeholderResolvers.add(placeholderResolver);
    }

    public void unregisterPlaceholderReplacer(PlaceholderResolver placeholderResolver) {
        Preconditions.checkArgument(!equals(placeholderResolver));
        Preconditions.checkState(placeholderResolvers.contains(placeholderResolver), "Tried to unregister a not registered placeholder replacer!");

        placeholderResolvers.remove(placeholderResolver);
    }

    @Override
    public String replacePlaceholders(OfflinePlayer player, String text) {
        for (PlaceholderResolver placeholderResolver : placeholderResolvers) {
            text = placeholderResolver.replacePlaceholders(player, text);
        }
        return text;
    }

    @Override
    public Set<String> getPlaceholders() {
        Set<String> placeholdersNames = new HashSet<>();

        for (PlaceholderResolver placeholderResolver : placeholderResolvers) {
            if (placeholderResolver == null) {
                continue;
            }

            placeholdersNames.addAll(placeholderResolver.getPlaceholders());
        }

        return ImmutableSet.copyOf(placeholdersNames);
    }

    @Override
    public Map<String, String> getPlaceholdersValues(OfflinePlayer player) {
        Map<String, String> map = new HashMap<>();

        for (PlaceholderResolver placeholderResolver : placeholderResolvers) {
            if (placeholderResolver == null) {
                continue;
            }

            map.putAll(placeholderResolver.getPlaceholdersValues(player));
        }

        return ImmutableMap.copyOf(map);
    }
}
