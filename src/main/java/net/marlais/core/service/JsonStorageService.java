package net.marlais.core.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.marlais.core.model.CorePlayer;
import net.marlais.core.model.SimpleCorePlayer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Реализация StorageService, сохраняющая данные в формате JSON на диск.
 */
public class JsonStorageService implements StorageService {

    private final File playersDirectory;
    private final Gson gson;

    public JsonStorageService(File dataFolder) {
        this.playersDirectory = new File(dataFolder, "players");
        // Красивое форматирование JSON с отступами
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void onEnable() throws Exception {
        if (!playersDirectory.exists() && !playersDirectory.mkdirs()) {
            throw new IOException("Не удалось создать папку для хранения игроков: " + playersDirectory.getAbsolutePath());
        }
    }

    @Override
    public void onDisable() throws Exception {
        // Здесь можно принудительно сбросить кэши на диск, если они появятся в будущем
    }

    @Override
    public CompletableFuture<CorePlayer> loadPlayer(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            File playerFile = new File(playersDirectory, uniqueId.toString() + ".json");
            
            if (!playerFile.exists()) {
                // Если файла нет, возвращаем «чистый» профиль по умолчанию
                return SimpleCorePlayer.builder(uniqueId, "Unknown").build();
            }

            try (FileReader reader = new FileReader(playerFile)) {
                // Читаем JSON и превращаем его в нашу Java-реализацию
                return gson.fromJson(reader, SimpleCorePlayer.class);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при загрузке игрока " + uniqueId, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> savePlayer(CorePlayer player) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = new File(playersDirectory, player.getUniqueId().toString() + ".json");
            
            try (FileWriter writer = new FileWriter(playerFile)) {
                // Сериализуем объект игрока в JSON файл
                gson.toJson(player, writer);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при сохранении игрока " + player.getUniqueId(), e);
            }
        });
    }
}