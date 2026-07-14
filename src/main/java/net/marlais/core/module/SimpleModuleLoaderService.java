package net.marlais.core.module;

import net.marlais.core.model.ModuleState;
import net.marlais.core.service.ServiceContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация загрузчика модулей с изолированной обработкой ошибок.
 */
public class SimpleModuleLoaderService implements ModuleLoaderService {

    private final ServiceContainer container;
    private final Map<String, CoreModule> modules = new ConcurrentHashMap<>();

    public SimpleModuleLoaderService(ServiceContainer container) {
        this.container = container;
    }

    @Override
    public void onEnable() throws Exception {
        // Логика инициализации самого загрузчика при запуске ядра
    }

    @Override
    public void onDisable() throws Exception {
        // При выключении ядра безопасно останавливаем все активные модули
        for (CoreModule module : modules.values()) {
            if (module.getState() == ModuleState.ACTIVE) {
                try {
                    module.onDisable();
                    module.setDisabled();
                } catch (Exception e) {
                    System.err.println("Ошибка при остановке модуля " + module.getName() + ": " + e.getMessage());
                    module.setFailed();
                }
            }
        }
        modules.clear();
    }

    @Override
    public void registerModule(CoreModule module) {
        if (modules.containsKey(module.getName())) {
            throw new IllegalStateException("Модуль с именем " + module.getName() + " уже зарегистрирован!");
        }
        module.init(container);
        modules.put(module.getName(), module);
    }

    @Override
    public void enableAllModules() {
        for (CoreModule module : modules.values()) {
            if (module.getState() != ModuleState.LOADED) {
                continue;
            }

            try {
                // Пытаемся активировать модуль
                module.onEnable();
                module.setActive();
            } catch (Exception e) {
                // Изолируем ошибку: модуль помечается как сломанный, но ядро продолжает жить
                System.err.println("КРИТИЧЕСКАЯ ОШИБКА: Не удалось запустить модуль " + module.getName());
                e.printStackTrace();
                module.setFailed();
            }
        }
    }

    @Override
    public Collection<CoreModule> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }
}