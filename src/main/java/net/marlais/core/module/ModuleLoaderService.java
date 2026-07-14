package net.marlais.core.module;

import net.marlais.core.service.Service;
import java.util.Collection;

public interface ModuleLoaderService extends Service {
    /** Регистрация модуля в системе. */
    void registerModule(CoreModule module);

    /** Активация всех зарегистрированных модулей. */
    void enableAllModules();

    /** Возвращает список всех зарегистрированных модулей. */
    Collection<CoreModule> getModules();
}