package me.fixeddev.nametag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public interface NametagApi {
    void setNametag(Player player, String prefix, String name, String suffix);

    default void setNametag(Player player, String nametag) {
        String name;
        String prefix = "";
        String suffix = "";

        if (nametag.length() <= 16) {
            name = nametag;
        } else if (nametag.length() <= 32) {
            prefix = nametag.substring(0, 16);
            name = nametag.substring(16);
        } else {
            prefix = nametag.substring(0, 16);
            name = nametag.substring(16, 32);
            suffix = nametag.substring(32, nametag.length() > 48 ? 48 : nametag.length());
        }

        setNametag(player, prefix, name, suffix);
    }


    @NotNull
    Tag getPlayerNametag(Player player);

    @NotNull
    static NametagApi createInstance(JavaPlugin plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            return new SimpleNametagApi();
        }

        return new ProtocolLibNametagApi(plugin);
    }
}
