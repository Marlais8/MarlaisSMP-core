package net.marlais.core.event;

import net.marlais.core.service.Service;

public interface EventBus extends Service {
    void registerListeners(Object listener);
    void unregisterListeners(Object listener);
    void callEvent(Event event);
}