package net.marlais.core.exception;

/**
 * Выбрасывается, когда запрашиваемая служба не найдена в ServiceContainer.
 */
public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String message) {
        super(message);
    }
}