package net.marlais.core.module;

import net.marlais.core.model.ModuleState;
import net.marlais.core.service.ServiceContainer;

/**
 * Абстрактный класс, который должен наследоваться каждым аддоном.
 */
public abstract class CoreModule {
    private final String name;
    private ModuleState state = ModuleState.DISCOVERED;
    private ServiceContainer container;

    protected CoreModule(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя модуля не может быть пустым");
        }
        this.name = name;
    }

    public String getName() { return name; }
    public ModuleState getState() { return state; }
    public ServiceContainer getContainer() { return container; }

    public void init(ServiceContainer container) {
        this.container = container;
        this.state = ModuleState.LOADED;
    }

    public void setFailed() { this.state = ModuleState.FAILED; }
    public void setDisabled() { this.state = ModuleState.DISABLED; }
    public void setActive() { this.state = ModuleState.ACTIVE; }

    /** Вызывается при запуске модуля. */
    public abstract void onEnable() throws Exception;

    /** Вызывается при отключении модуля. */
    public abstract void onDisable() throws Exception;
}