package net.marlais.core.service;

import net.marlais.core.model.CorePlayer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс службы для асинхронного сохранения и загрузки данных.
 */
public interface StorageService extends Service {
    /**
     * Асинхронно загружает профиль игрока из хранилища.
     */
    CompletableFuture<CorePlayer> loadPlayer(UUID uniqueId);

    /**
     * Асинхронно сохраняет профиль игрока в хранилище.
     */
    CompletableFuture<Void> savePlayer(CorePlayer player);
}