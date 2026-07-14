package net.marlais.core.service;

public interface Service {
    /**
     * Вызывается при активации службы.
     */
    void onEnable() throws Exception;

    /**
     * Вызывается при остановке службы для освобождения ресурсов.
     */
    void onDisable() throws Exception;
}