package net.marlais.core.service;

import net.marlais.core.exception.ServiceNotFoundException;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Потокобезопасная реализация ServiceContainer.
 */
public class SimpleServiceContainer implements ServiceContainer {
    
    private final Map<Class<? extends Service>, Service> services = new ConcurrentHashMap<>();
    
    // Используем Stack (он потокобезопасен в Java), чтобы останавливать службы 
    // в порядке, обратном их регистрации (LIFO).
    private final Stack<Service> shutdownOrder = new Stack<>();

    @Override
    public <T extends Service> void register(Class<T> serviceClass, T implementation) {
        services.put(serviceClass, implementation);
        shutdownOrder.push(implementation);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Service> T get(Class<T> serviceClass) {
        Service service = services.get(serviceClass);
        if (service == null) {
            throw new ServiceNotFoundException("Служба не зарегистрирована: " + serviceClass.getSimpleName());
        }
        return (T) service;
    }

    @Override
    public void unregister(Class<? extends Service> serviceClass) {
        Service service = services.remove(serviceClass);
        if (service != null) {
            shutdownOrder.remove(service);
        }
    }

    @Override
    public void shutdownAll() {
        while (!shutdownOrder.isEmpty()) {
            Service service = shutdownOrder.pop();
            try {
                service.onDisable();
            } catch (Exception e) {
                // В будущем здесь будет системный логгер
                System.err.println("Ошибка при остановке службы " + service.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        services.clear();
    }
}