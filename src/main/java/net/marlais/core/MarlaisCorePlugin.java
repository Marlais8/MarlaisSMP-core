package net.marlais.core;

import net.marlais.core.event.AsynchronousEventBus;
import net.marlais.core.event.EventBus;
import net.marlais.core.event.PlayerJoinEvent;
import net.marlais.core.model.CorePlayer;
import net.marlais.core.module.AdminToolModule;
import net.marlais.core.module.ModuleLoaderService;
import net.marlais.core.module.SimpleModuleLoaderService;
import net.marlais.core.service.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public final class MarlaisCorePlugin extends JavaPlugin implements Listener, CommandExecutor {

    private ServiceContainer container;
    private AdminToolModule adminModule;

    @Override
    public void onEnable() {
        // Инициализируем наш контейнер сервисов
        container = new SimpleServiceContainer();

        // Регистрируем сервисы ядра
        EventBus eventBus = new AsynchronousEventBus();
        StorageService storageService = new JsonStorageService(getDataFolder());
        ScoreboardService scoreboardService = new SimpleScoreboardService(); // В реальной жизни тут будет Bukkit Scoreboard
        ModuleLoaderService moduleLoader = new SimpleModuleLoaderService(container);

        container.register(EventBus.class, eventBus);
        container.register(StorageService.class, storageService);
        container.register(ScoreboardService.class, scoreboardService);
        container.register(ModuleLoaderService.class, moduleLoader);

        try {
            // Включаем сервисы
            eventBus.onEnable();
            storageService.onEnable();
            scoreboardService.onEnable();
            moduleLoader.onEnable();

            // Регистрируем наш аддон
            adminModule = new AdminToolModule();
            moduleLoader.registerModule(adminModule);
            moduleLoader.enableAllModules();

        } catch (Exception e) {
            getLogger().severe("Не удалось запустить MarlaisCore!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Регистрируем Bukkit-слушатель для перенаправления событий в нашу шину событий
        getServer().getPluginManager().registerEvents(this, this);

        // Регистрируем команды в Bukkit
        getCommand("depositdollars").setExecutor(this);
        getCommand("depositcoins").setExecutor(this);

        getLogger().info("MarlaisCore успешно запущен в игре!");
    }

    @Override
    public void onDisable() {
        if (container != null) {
            container.shutdownAll();
        }
    }

    // Слушаем реальный вход игрока в Minecraft и перенаправляем в нашу систему
    @org.bukkit.event.EventHandler
    public void onBukkitJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        StorageService storage = container.get(StorageService.class);
        EventBus eventBus = container.get(EventBus.class);

        // Загружаем профиль асинхронно
        storage.loadPlayer(player.getUniqueId()).thenAccept(corePlayer -> {
            // Если игрок зашел впервые, обновим его имя в профиле
            if (corePlayer.getLastKnownName().equals("Unknown")) {
                corePlayer = net.marlais.core.model.SimpleCorePlayer.builder(corePlayer)
                        .firstPlay(System.currentTimeMillis())
                        .build();
            }

            // Создаем наше внутреннее событие и пускаем в нашу шину
            final CorePlayer finalPlayer = corePlayer;
            Bukkit.getScheduler().runTask(this, () -> {
                eventBus.callEvent(new PlayerJoinEvent(finalPlayer));
            });
        });
    }

    // Обработка команд администратора из игры или консоли
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /" + label + " <игрок> <количество>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cИгрок не найден в сети!");
            return true;
        }

        UUID uuid = target.getUniqueId();

        try {
            if (command.getName().equalsIgnoreCase("depositdollars")) {
                double amount = Double.parseDouble(args[1]);
                adminModule.cmdDepositDollars(uuid, amount);
                sender.sendMessage("§aВы выдали " + target.getName() + " " + amount + "$");
            } else if (command.getName().equalsIgnoreCase("depositcoins")) {
                long amount = Long.parseLong(args[1]);
                adminModule.cmdDepositCoins(uuid, amount);
                sender.sendMessage("§aВы выдали " + target.getName() + " " + amount + " коинов");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cПожалуйста, введите корректное число!");
        }

        return true;
    }
}