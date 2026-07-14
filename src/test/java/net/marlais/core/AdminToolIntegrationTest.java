package net.marlais.core;

import net.marlais.core.event.AsynchronousEventBus;
import net.marlais.core.event.EventBus;
import net.marlais.core.event.PlayerJoinEvent;
import net.marlais.core.model.CorePlayer;
import net.marlais.core.model.SimpleCorePlayer;
import net.marlais.core.module.AdminToolModule;
import net.marlais.core.module.ModuleLoaderService;
import net.marlais.core.module.SimpleModuleLoaderService;
import net.marlais.core.service.*;
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
    private ScoreboardService scoreboardService;
    private ModuleLoaderService moduleLoader;

    @BeforeEach
    void setUp() throws Exception {
        container = new SimpleServiceContainer();
        
        eventBus = new AsynchronousEventBus();
        storageService = new JsonStorageService(tempDir);
        scoreboardService = new SimpleScoreboardService(); // Новая служба табло
        moduleLoader = new SimpleModuleLoaderService(container);

        container.register(EventBus.class, eventBus);
        container.register(StorageService.class, storageService);
        container.register(ScoreboardService.class, scoreboardService);
        container.register(ModuleLoaderService.class, moduleLoader);

        // Активируем службы ядра
        eventBus.onEnable();
        storageService.onEnable();
        scoreboardService.onEnable();
        moduleLoader.onEnable();
    }

    @AfterEach
    void tearDown() throws Exception {
        container.shutdownAll();
    }

    @Test
    void testAdminCoreIntegration() {
        AdminToolModule adminModule = new AdminToolModule();
        moduleLoader.registerModule(adminModule);
        moduleLoader.enableAllModules();

        UUID playerUuid = UUID.randomUUID();
        String playerName = "Fedir";

        // Создаем дефолтного игрока в базе данных
        CorePlayer initPlayer = SimpleCorePlayer.builder(playerUuid, playerName)
                .addMetadata("economy:dollars", "100.00")
                .addMetadata("economy:coins", "50")
                .build();
        storageService.savePlayer(initPlayer).join();

        // 1. Имитируем вход игрока на сервер (вызов события) — табло должно отрисоваться
        System.out.println("\n--- ИМИТАЦИЯ ВХОДА ИГРОКА ---");
        CorePlayer activePlayer = storageService.loadPlayer(playerUuid).join();
        eventBus.callEvent(new PlayerJoinEvent(activePlayer));

        // 2. Имитируем использование админ-команды (пополнение баланса $) — табло должно обновиться!
        System.out.println("\n--- ИСПОЛЬЗОВАНИЕ АДМИН-КОМАНДЫ ---");
        adminModule.cmdDepositDollars(playerUuid, 250.50);

        // Проверяем, что новый баланс успешно записался на диск
        CorePlayer finalPlayer = storageService.loadPlayer(playerUuid).join();
        assertEquals("350.5", finalPlayer.getMetadataValue("economy:dollars").orElse("0"));
    }
}