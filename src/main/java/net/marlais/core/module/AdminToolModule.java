package net.marlais.core.module;

import net.marlais.core.event.EventHandler;
import net.marlais.core.event.PlayerJoinEvent;
import net.marlais.core.model.CorePlayer;
import net.marlais.core.model.SimpleCorePlayer;
import net.marlais.core.service.StorageService;

import java.util.UUID;

/**
 * Модуль администратора для управления балансом игроков.
 */
public class AdminToolModule extends CoreModule {

    public AdminToolModule() {
        super("AdminTool");
    }

    @Override
    public void onEnable() throws Exception {
        // Регистрируем этот класс как слушатель событий в нашей шине
        getContainer().get(net.marlais.core.event.EventBus.class).registerListeners(this);
        System.out.println("[AdminTool] Модуль успешно активирован!");
    }

    @Override
    public void onDisable() throws Exception {
        getContainer().get(net.marlais.core.event.EventBus.class).unregisterListeners(this);
        System.out.println("[AdminTool] Модуль отключен.");
    }

    /**
     * Обработчик события входа игрока.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CorePlayer player = event.getPlayer();
        // Получаем баланс из метаданных (по умолчанию "0")
        String dollarBalance = player.getMetadataValue("economy:dollars").orElse("0");
        String coinBalance = player.getMetadataValue("economy:coins").orElse("0");

        // Выводим информацию в консоль администратора
        System.out.println("[MarlaisCore] Игрок " + player.getLastKnownName() + " вошел на сервер.");
        System.out.println("[MarlaisCore] Баланс " + player.getLastKnownName() + ": " + dollarBalance + "$ | " + coinBalance + " Coins");
    }

    /**
     * Административная команда: Пополнить баланс в долларах ($)
     */
    public void cmdDepositDollars(UUID uuid, double amount) {
        StorageService storage = getContainer().get(StorageService.class);

        // Асинхронно загружаем, меняем баланс и сохраняем обратно
        storage.loadPlayer(uuid).thenAccept(player -> {
            double current = Double.parseDouble(player.getMetadataValue("economy:dollars").orElse("0"));
            double updated = current + amount;

            CorePlayer updatedPlayer = SimpleCorePlayer.builder(player)
                    .addMetadata("economy:dollars", String.valueOf(updated))
                    .build();

            storage.savePlayer(updatedPlayer).thenRun(() -> {
                System.out.println("[Admin Command] Баланс игрока " + player.getLastKnownName() + " успешно пополнен на " + amount + "$. Новый баланс: " + updated + "$");
            });
        }).join();
    }

    /**
     * Административная команда: Пополнить баланс в Coins
     */
    public void cmdDepositCoins(UUID uuid, long amount) {
        StorageService storage = getContainer().get(StorageService.class);

        storage.loadPlayer(uuid).thenAccept(player -> {
            long current = Long.parseLong(player.getMetadataValue("economy:coins").orElse("0"));
            long updated = current + amount;

            CorePlayer updatedPlayer = SimpleCorePlayer.builder(player)
                    .addMetadata("economy:coins", String.valueOf(updated))
                    .build();

            storage.savePlayer(updatedPlayer).thenRun(() -> {
                System.out.println("[Admin Command] Баланс игрока " + player.getLastKnownName() + " успешно пополнен на " + amount + " Coins. Новый баланс: " + updated + " Coins");
            });
        }).join();
    }
}