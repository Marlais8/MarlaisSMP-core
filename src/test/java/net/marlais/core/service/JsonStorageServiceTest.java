package net.marlais.core.service;

import net.marlais.core.model.CorePlayer;
import net.marlais.core.model.SimpleCorePlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JsonStorageServiceTest {

    // JUnit автоматически создаст временную папку для тестов и удалит её после них
    @TempDir
    File tempDir;

    private JsonStorageService storageService;

    @BeforeEach
    void setUp() throws Exception {
        storageService = new JsonStorageService(tempDir);
        storageService.onEnable();
    }

    @AfterEach
    void tearDown() throws Exception {
        storageService.onDisable();
    }

    @Test
    void testSaveAndLoadPlayer() throws Exception {
        UUID uuid = UUID.randomUUID();
        String name = "TestCodex";

        CorePlayer originalPlayer = SimpleCorePlayer.builder(uuid, name)
                .addMetadata("coins", "1500")
                .addMetadata("rank", "VIP")
                .build();

        // Сохраняем игрока и ждем завершения асинхронного потока (.join())
        storageService.savePlayer(originalPlayer).join();

        // Загружаем обратно
        CorePlayer loadedPlayer = storageService.loadPlayer(uuid).join();

        // Проверяем, что все данные совпали
        assertNotNull(loadedPlayer);
        assertEquals(originalPlayer.getUniqueId(), loadedPlayer.getUniqueId());
        assertEquals(originalPlayer.getLastKnownName(), loadedPlayer.getLastKnownName());
        assertEquals("1500", loadedPlayer.getMetadataValue("coins").orElse("0"));
        assertEquals("VIP", loadedPlayer.getMetadataValue("rank").orElse("DEFAULT"));
    }

    @Test
    void testLoadNonExistentPlayer() {
        UUID uuid = UUID.randomUUID();
        
        // Попытка загрузить несуществующего игрока должна вернуть дефолтный профиль
        CorePlayer player = storageService.loadPlayer(uuid).join();
        
        assertNotNull(player);
        assertEquals(uuid, player.getUniqueId());
        assertEquals("Unknown", player.getLastKnownName());
    }
}