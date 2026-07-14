package net.marlais.core.service;

public interface ServiceContainer {
    <T extends Service> void register(Class<T> serviceClass, T implementation);

    <T extends Service> T get(Class<T> serviceClass);

    void unregister(Class<? extends Service> serviceClass);

    void shutdownAll();
}