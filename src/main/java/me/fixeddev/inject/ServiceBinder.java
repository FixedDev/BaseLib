package me.fixeddev.inject;

import com.google.inject.Binder;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import me.fixeddev.service.Service;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ServiceBinder {

    private Multibinder<Service> serviceMultibinder;

    private Binder binder;

    public ServiceBinder(Binder binder) {
        serviceMultibinder = Multibinder.newSetBinder(binder, Service.class);

        this.binder = binder;
    }

    public void bindService(Service service) {
        serviceMultibinder.addBinding().toInstance(service);
    }

    /**
     * This binds the selected class into a Multibinder<Service> so we can inject Set<Service>
     *     and enable all the services on demand
     *
     * @param serviceClass - The class of the service to bind
     * @param scope - The selected scope if null defaults to Scopes.NO_SCOPE
     */
    public void bindService(Class<Service> serviceClass, @Nullable Scope scope) {
        serviceMultibinder.addBinding().to(serviceClass).in(Optional.ofNullable(scope)
                .orElse(Scopes.NO_SCOPE));
    }
}
