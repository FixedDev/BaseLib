

package me.fixeddev.scoreboard.placeholder;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class ListPlaceholderResolver implements PlaceholderResolver {

    private Map<String, String> placeholderValues;

    @Override
    public String replacePlaceholders(OfflinePlayer player, String text) {
        if(StringUtils.isBlank(text)){
            return text;
        }

        for (Map.Entry<String, String> entry : placeholderValues.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    @Override
    public Set<String> getPlaceholders() {
        return placeholderValues.keySet();
    }

    @Override
    public Map<String, String> getPlaceholdersValues(OfflinePlayer player) {
        return ImmutableMap.copyOf(placeholderValues);
    }
}
