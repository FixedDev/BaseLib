package me.fixeddev.inject;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import me.fixeddev.service.PluginService;
import me.fixeddev.service.Service;
import me.fixeddev.bcm.parametric.ParametricCommandHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;

import java.util.Set;
import java.util.logging.Level;

@ImplementedBy(ServiceManager.ServiceManagerImpl.class)
public interface ServiceManager {
    void start();

    void stop();

    class ServiceManagerImpl implements ServiceManager {

        @Inject
        private Set<Service> services;
        @Inject
        private PluginLogger logger;
        @Inject
        private PluginManager pluginManager;
        @Inject
        private Plugin plugin;
        @Inject
        private ParametricCommandHandler commandHandler;

        @Override
        public void start() {
            for (Service service : services) {

                try {
                    if (service instanceof PluginService) {
                        PluginService pluginService = (PluginService) service;

                        pluginService.registerCommands(commandHandler);
                        pluginService.registerListeners(plugin,pluginManager);
                    }

                    service.start();
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "An error ocurred while a service was being enabled", ex);
                }
            }
        }

        @Override
        public void stop() {
            for (Service service : services) {
                service.stop();
            }
        }
    }
}
