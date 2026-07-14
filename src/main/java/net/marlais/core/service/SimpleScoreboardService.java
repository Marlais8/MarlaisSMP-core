package net.marlais.core.service;

import net.marlais.core.model.CorePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * Реальная служба управления игровым табло (Scoreboard) на экране.
 */
public class SimpleScoreboardService implements ScoreboardService {

    @Override
    public void onEnable() throws Exception {
        System.out.println("[Scoreboard] Система игровых табло успешно запущена.");
    }

    @Override
    public void onDisable() throws Exception {
        System.out.println("[Scoreboard] Система игровых табло остановлена.");
    }

    @Override
    public void updateSidebar(CorePlayer player) {
        // 1. Пытаемся найти реального онлайн-игрока по его UUID
        Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
        if (bukkitPlayer == null || !bukkitPlayer.isOnline()) {
            return; // Игрок не в сети, обновлять некого
        }

        String name = player.getLastKnownName();
        if (name == null || name.equalsIgnoreCase("Unknown")) {
            name = bukkitPlayer.getName(); // Берем ник напрямую из игры, если в базе еще "Unknown"
        }

        String dollarBalance = player.getMetadataValue("economy:dollars").orElse("0.00");
        String coinBalance = player.getMetadataValue("economy:coins").orElse("0");

        // 2. Получаем менеджер скорбордов сервера
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        // 3. Создаем новое индивидуальное табло для игрока
        Scoreboard board = manager.getNewScoreboard();

        // Регистрируем задачу на боковую панель (SIDEBAR) с красивым золотым заголовком
        Objective obj = board.registerNewObjective("marlais_sb", "dummy", "§6§lMarlaisSMP");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // 4. Заполняем строчки табло (в обратном порядке индексов)
        obj.getScore("§7-----------------").setScore(5);
        obj.getScore("§fИгрок: §a" + name).setScore(4);
        obj.getScore("§fБаланс: §e" + dollarBalance + " $").setScore(3);
        obj.getScore("§fМонеты: §6" + coinBalance + " Coins").setScore(2);
        obj.getScore("§7=================").setScore(1);

        // 5. Устанавливаем скорборд игроку на экран
        bukkitPlayer.setScoreboard(board);
    }
}