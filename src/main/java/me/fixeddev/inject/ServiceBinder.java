package me.fixeddev.inject;

import com.google.inject.Binder;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import me.fixeddev.service.Service;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Inspired by the ProjectAres PluginFacetBinder <3
 */
public class ServiceBinder {

    private Multibinder<Service> serviceMultibinder;

    private Binder binder;

    public ServiceBinder(Binder binder) {
        serviceMultibinder = Multibinder.newSetBinder(binder, Service.class);

        this.binder = binder;
    }

    /**
     * This is not ensured to work
     * @param service - The service to bind
     */
    public void bindService(Service service) {
        serviceMultibinder.addBinding().toInstance(service);

        binder.bind((Class<Service>) service.getClass()).toInstance(service);
    }

    /**
     * This binds the selected class into a Multibinder<Service> so we can inject Set<Service>
     * and enable all the services on demand
     *
     * @param serviceClass - The class of the service to bind
     * @param scope        - The selected scope if null defaults to Scopes.NO_SCOPE
     */
    public void bindService(Class<Service> serviceClass, @Nullable Scope scope) {
        serviceMultibinder.addBinding().to(serviceClass);

        binder.bind(serviceClass).in(Optional.ofNullable(scope)
                .orElse(Scopes.NO_SCOPE));
    }
}
