package net.marlais.core.event;

/**
 * Базовый класс для всех событий ядра.
 */
public abstract class Event {
    private final boolean async;

    protected Event(boolean async) {
        this.async = async;
    }

    public boolean isAsync() {
        return async;
    }
}