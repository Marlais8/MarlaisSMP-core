package net.marlais.core.event;

import net.marlais.core.model.CorePlayer;

/**
 * Событие, вызываемое при входе игрока на сервер.
 */
public class PlayerJoinEvent extends Event {
    private final CorePlayer player;

    public PlayerJoinEvent(CorePlayer player) {
        super(false); // Синхронное событие
        this.player = player;
    }

    public CorePlayer getPlayer() {
        return player;
    }
}