package net.marlais.core.module;

import net.marlais.core.model.ModuleState;
import net.marlais.core.service.ServiceContainer;
import net.marlais.core.service.SimpleServiceContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleLoaderTest {

    private ServiceContainer container;
    private SimpleModuleLoaderService loaderService;

    @BeforeEach
    void setUp() {
        container = new SimpleServiceContainer();
        loaderService = new SimpleModuleLoaderService(container);
    }

    // Мок рабочего модуля
    static class DummyModule extends CoreModule {
        boolean enabled = false;
        boolean disabled = false;

        protected DummyModule(String name) { super(name); }

        @Override
        public void onEnable() { enabled = true; }

        @Override
        public void onDisable() { disabled = true; }
    }

    // Мок проблемного модуля
    static class BrokenModule extends CoreModule {
        protected BrokenModule(String name) { super(name); }

        @Override
        public void onEnable() throws Exception {
            throw new RuntimeException("Упс! Ошибка конфигурации базы данных.");
        }

        @Override
        public void onDisable() {}
    }

    @Test
    void testSuccessfulLifecycle() {
        DummyModule module = new DummyModule("TestAddon");
        assertEquals(ModuleState.DISCOVERED, module.getState());

        loaderService.registerModule(module);
        assertEquals(ModuleState.LOADED, module.getState());
        assertSame(container, module.getContainer());

        loaderService.enableAllModules();
        assertEquals(ModuleState.ACTIVE, module.getState());
        assertTrue(module.enabled);
    }

    @Test
    void testModuleFailureIsolation() {
        DummyModule workingModule = new DummyModule("HealthyModule");
        BrokenModule brokenModule = new BrokenModule("CrashModule");

        loaderService.registerModule(workingModule);
        loaderService.registerModule(brokenModule);

        // Запускаем все модули
        loaderService.enableAllModules();

        // Проверяем изоляцию ошибок:
        // Сбойный модуль должен уйти в FAILED
        assertEquals(ModuleState.FAILED, brokenModule.getState());
        // Рабочий модуль должен успешно запуститься и перейти в ACTIVE
        assertEquals(ModuleState.ACTIVE, workingModule.getState());
        assertTrue(workingModule.enabled);
    }
}