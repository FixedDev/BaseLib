package me.fixeddev.nametag;

import org.bukkit.entity.Player;

public class SimpleNametagApi implements NametagApi {
    @Override
    public void setNametag(Player player, String nametag) {
        player.setPlayerListName(nametag);
    }

    @Override
    public String getPlayerNametag(Player player) {
        return player.getPlayerListName();
    }
}
