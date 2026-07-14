package net.marlais.core.model;

import java.util.UUID;
import java.util.Map;
import java.util.Optional;

/**
 * Представляет профиль игрока в системе MarlaisSMP Core.
 */
public interface CorePlayer {
    /**
     * Уникальный UUID игрока.
     */
    UUID getUniqueId();

    /**
     * Последний известный никнейм игрока.
     */
    String getLastKnownName();

    /**
     * Время первого входа на сервер (timestamp в миллисекундах).
     */
    long getFirstPlay();

    /**
     * Время последнего входа на сервер.
     */
    long getLastPlay();

    /**
     * Получить метаданные, привязанные к игроку (например, уровень, баланс).
     */
    Map<String, String> getMetadata();

    /**
     * Безопасное получение конкретного значения из метаданных.
     */
    Optional<String> getMetadataValue(String key);
}