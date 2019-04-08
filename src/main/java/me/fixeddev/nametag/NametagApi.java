package me.fixeddev.nametag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface NametagApi {
    void setNametag(Player player, String nametag);

    String getPlayerNametag(Player player);

    static NametagApi createInstance(JavaPlugin plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            return new SimpleNametagApi();
        }

        return new ProtocolLibNametagApi(plugin);
    }
}
