package me.fixeddev.scoreboard.placeholder;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.Set;

public interface PlaceholderResolver {

    String replacePlaceholders(OfflinePlayer player, String text);

    Set<String> getPlaceholders();

    Map<String,String> getPlaceholdersValues(OfflinePlayer player);


}
