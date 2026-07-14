package net.marlais.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Неизменяемая (Immutable) реализация профиля игрока с паттерном Builder.
 */
public final class SimpleCorePlayer implements CorePlayer {

    private final UUID uniqueId;
    private final String lastKnownName;
    private final long firstPlay;
    private final long lastPlay;
    private final Map<String, String> metadata;

    private SimpleCorePlayer(Builder builder) {
        this.uniqueId = builder.uniqueId;
        this.lastKnownName = builder.lastKnownName;
        this.firstPlay = builder.firstPlay;
        this.lastPlay = builder.lastPlay;
        // Защищаем мапу от изменений извне (условие Иммутабельности)
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
    }

    @Override
    public UUID getUniqueId() { return uniqueId; }

    @Override
    public String getLastKnownName() { return lastKnownName; }

    @Override
    public long getFirstPlay() { return firstPlay; }

    @Override
    public long getLastPlay() { return lastPlay; }

    @Override
    public Map<String, String> getMetadata() { return metadata; }

    @Override
    public Optional<String> getMetadataValue(String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    /**
     * Создает новый Builder для сборки объекта игрока.
     */
    public static Builder builder(UUID uniqueId, String lastKnownName) {
        return new Builder(uniqueId, lastKnownName);
    }

    /**
     * Позволяет создать копию игрока с обновленными данными.
     */
    public static Builder builder(CorePlayer player) {
        return new Builder(player.getUniqueId(), player.getLastKnownName())
                .firstPlay(player.getFirstPlay())
                .lastPlay(player.getLastPlay())
                .metadata(player.getMetadata());
    }

    public static class Builder {
        private final UUID uniqueId;
        private final String lastKnownName;
        private long firstPlay = System.currentTimeMillis();
        private long lastPlay = System.currentTimeMillis();
        private Map<String, String> metadata = new HashMap<>();

        private Builder(UUID uniqueId, String lastKnownName) {
            if (uniqueId == null || lastKnownName == null) {
                throw new IllegalArgumentException("UUID и никнейм не могут быть null");
            }
            this.uniqueId = uniqueId;
            this.lastKnownName = lastKnownName;
        }

        public Builder firstPlay(long firstPlay) {
            this.firstPlay = firstPlay;
            return this;
        }

        public Builder lastPlay(long lastPlay) {
            this.lastPlay = lastPlay;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = new HashMap<>(metadata);
            return this;
        }

        public Builder addMetadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public SimpleCorePlayer build() {
            return new SimpleCorePlayer(this);
        }
    }
}