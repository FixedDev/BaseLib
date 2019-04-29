package me.fixeddev.inject;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import me.fixeddev.service.Service;
import org.bukkit.plugin.PluginLogger;

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

        @Override
        public void start() {
            for (Service service : services) {
                try {
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
