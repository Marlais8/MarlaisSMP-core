package net.marlais.core.service;

import net.marlais.core.model.CorePlayer;

/**
 * Служба для управления боковым табло (Sidebar) игроков.
 */
public interface ScoreboardService extends Service {
    /**
     * Создает или обновляет боковое меню для конкретного игрока.
     */
    void updateSidebar(CorePlayer player);
}