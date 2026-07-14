package net.marlais.core.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CorePlayerTest {

    @Test
    void testPlayerBuilderAndGetters() {
        UUID uuid = UUID.randomUUID();
        String name = "Marlais8";
        
        SimpleCorePlayer player = SimpleCorePlayer.builder(uuid, name)
                .addMetadata("addon_economy:balance", "500")
                .build();

        assertEquals(uuid, player.getUniqueId());
        assertEquals(name, player.getLastKnownName());
        assertEquals("500", player.getMetadataValue("addon_economy:balance").orElse("0"));
    }

    @Test
    void testImmutableMetadata() {
        UUID uuid = UUID.randomUUID();
        SimpleCorePlayer player = SimpleCorePlayer.builder(uuid, "TestPlayer").build();

        // Проверяем, что мапу нельзя изменить напрямую (выбросит UnsupportedOperationException)
        assertThrows(UnsupportedOperationException.class, () -> {
            player.getMetadata().put("illegal", "value");
        });
    }

    @Test
    void testPlayerCopyAndModify() {
        UUID uuid = UUID.randomUUID();
        SimpleCorePlayer playerV1 = SimpleCorePlayer.builder(uuid, "Player")
                .addMetadata("level", "1")
                .build();

        // Создаем копию с измененными метаданными (Паттерн для иммутабельных структур)
        SimpleCorePlayer playerV2 = SimpleCorePlayer.builder(playerV1)
                .addMetadata("level", "2")
                .build();

        assertEquals("1", playerV1.getMetadataValue("level").orElse("0"));
        assertEquals("2", playerV2.getMetadataValue("level").orElse("0"));
    }
}