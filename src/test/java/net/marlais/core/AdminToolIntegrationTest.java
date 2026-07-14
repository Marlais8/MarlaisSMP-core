package net.marlais.core;

import net.marlais.core.event.AsynchronousEventBus;
import net.marlais.core.event.EventBus;
import net.marlais.core.event.PlayerJoinEvent;
import net.marlais.core.model.CorePlayer;
import net.marlais.core.model.SimpleCorePlayer;
import net.marlais.core.module.AdminToolModule;
import net.marlais.core.module.ModuleLoaderService;
import net.marlais.core.module.SimpleModuleLoaderService;
import net.marlais.core.service.JsonStorageService;
import net.marlais.core.service.ServiceContainer;
import net.marlais.core.service.SimpleServiceContainer;
import net.marlais.core.service.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminToolIntegrationTest {

    @TempDir
    File tempDir;

    private ServiceContainer container;
    private EventBus eventBus;
    private StorageService storageService;
    private ModuleLoaderService moduleLoader;

    @BeforeEach
    void setUp() throws Exception {
        container = new SimpleServiceContainer();
        
        // 1. Создаем и регистрируем системные службы
        eventBus = new AsynchronousEventBus();
        storageService = new JsonStorageService(tempDir);
        moduleLoader = new SimpleModuleLoaderService(container);

        container.register(EventBus.class, eventBus);
        container.register(StorageService.class, storageService);
        container.register(ModuleLoaderService.class, moduleLoader);

        // Активируем службы ядра
        eventBus.onEnable();
        storageService.onEnable();
        moduleLoader.onEnable();
    }

    @AfterEach
    void tearDown() throws Exception {
        container.shutdownAll();
    }

    @Test
    void testAdminCoreIntegration() {
        // 2. Регистрируем наш модуль управления
        AdminToolModule adminModule = new AdminToolModule();
        moduleLoader.registerModule(adminModule);
        moduleLoader.enableAllModules();

        UUID playerUuid = UUID.randomUUID();
        String playerName = "Fedir";

        // Создаем дефолтного игрока в базе данных
        CorePlayer initPlayer = SimpleCorePlayer.builder(playerUuid, playerName)
                .addMetadata("economy:dollars", "100.0")
                .addMetadata("economy:coins", "50")
                .build();
        storageService.savePlayer(initPlayer).join();

        // 3. Имитируем вход игрока на сервер (вызов события)
        System.out.println("\n--- ИМИТАЦИЯ ВХОДА ИГРОКА ---");
        CorePlayer activePlayer = storageService.loadPlayer(playerUuid).join();
        eventBus.callEvent(new PlayerJoinEvent(activePlayer));

        // 4. Имитируем использование админ-команд
        System.out.println("\n--- ИСПОЛЬЗОВАНИЕ АДМИН-КОМАНД ---");
        adminModule.cmdDepositDollars(playerUuid, 250.50);
        adminModule.cmdDepositCoins(playerUuid, 1000);

        // Проверяем, что новые балансы успешно записались на диск
        CorePlayer finalPlayer = storageService.loadPlayer(playerUuid).join();
        assertEquals("350.5", finalPlayer.getMetadataValue("economy:dollars").orElse("0"));
        assertEquals("1050", finalPlayer.getMetadataValue("economy:coins").orElse("0"));
        System.out.println("---------------------------------\n");
    }
}