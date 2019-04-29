package me.fixeddev.service;

import me.ggamer55.bcm.parametric.ParametricCommandHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public interface PluginService extends Service {
    default void registerCommands(ParametricCommandHandler commandHandler) {
    }

    default void registerListeners(Plugin plugin, PluginManager pluginManager) {
    }
}
