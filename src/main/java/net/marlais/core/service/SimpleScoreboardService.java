package net.marlais.core.service;

import net.marlais.core.model.CorePlayer;

/**
 * Имитация службы управления игровым табло (Scoreboard).
 */
public class SimpleScoreboardService implements ScoreboardService {

    @Override
    public void onEnable() throws Exception {
        System.out.println("[Scoreboard] Система игровых табло запущена.");
    }

    @Override
    public void onDisable() throws Exception {
        System.out.println("[Scoreboard] Система игровых табло остановлена.");
    }

    @Override
    public void updateSidebar(CorePlayer player) {
        String name = player.getLastKnownName();
        String dollarBalance = player.getMetadataValue("economy:dollars").orElse("0.00");
        String coinBalance = player.getMetadataValue("economy:coins").orElse("0");

        // Имитируем то, как данные улетают в игровой Scoreboard игрока
        System.out.println("\n===== [ОТПРАВКА ИГРОВОГО SCOREBOARD ДЛЯ " + name.toUpperCase() + "] =====");
        System.out.println("  MarlaisSMP  ");
        System.out.println("-----------------");
        System.out.println(" Игрок: " + name);
        System.out.println(" Баланс: " + dollarBalance + " $");
        System.out.println(" Монеты: " + coinBalance + " Coins");
        System.out.println("===================================================\n");
    }
}