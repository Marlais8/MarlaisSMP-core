# 05_MODULE_LOADER.md

## 1. Назначение документа
Этот документ описывает архитектуру и жизненный цикл загрузчика модулей (Module Loader) для MarlaisSMP Core. Загрузчик отвечает за поиск, валидацию, загрузку, активацию и выключение внешних аддонов (модулей).

## 2. Архитектурные требования
- **Изоляция жизненного цикла:** Каждый модуль должен проходить строгие фазы жизненного цикла: `DISCOVERED` -> `LOADED` -> `ACTIVE` -> `DISABLED` (или `FAILED` в случае ошибок).
- **Безопасный запуск:** Ошибка при активации одного модуля не должна ломать работу всего ядра или других аддонов.
- **Интеграция с сервисами:** При активации модуль должен автоматически получать доступ к `ServiceContainer` ядра, чтобы иметь возможность работать с базой данных (`StorageService`) и регистрировать свои события в `EventBus`.

## 3. Базовый класс модуля (CoreModule)
Каждый разрабатываемый аддон должен наследоваться от этого абстрактного класса.

```java
package net.marlais.core.module;

import net.marlais.core.model.ModuleState;
import net.marlais.core.service.ServiceContainer;

public abstract class CoreModule {
    private final String name;
    private ModuleState state = ModuleState.DISCOVERED;
    private ServiceContainer container;

    protected CoreModule(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public ModuleState getState() { return state; }
    public ServiceContainer getContainer() { return container; }

    // Внутренние методы для управления состоянием со стороны ядра
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
```

## 4. Интерфейс ModuleLoaderService
Служба, которая сканирует систему, загружает модули и управляет их жизненным циклом.

```java
package net.marlais.core.module;

import net.marlais.core.service.Service;
import java.util.Collection;

public interface ModuleLoaderService extends Service {
    /**
     * Регистрирует модуль в системе вручную (для тестов и внутренней инициализации).
     */
    void registerModule(CoreModule module);

    /**
     * Запускает все зарегистрированные модули.
     */
    void enableAllModules();

    /**
     * Возвращает список всех зарегистрированных модулей.
     */
    Collection<CoreModule> getModules();
}
```