package net.marlais.core.service;

import net.marlais.core.exception.ServiceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ServiceContainerTest {

    private ServiceContainer container;

    @BeforeEach
    void setUp() {
        container = new SimpleServiceContainer();
    }

    // Мок-служба для тестов
    static class DummyService implements Service {
        boolean enabled = false;
        boolean disabled = false;

        @Override
        public void onEnable() { enabled = true; }

        @Override
        public void onDisable() { disabled = true; }
    }

    @Test
    void testRegisterAndGet() {
        DummyService service = new DummyService();
        container.register(DummyService.class, service);

        DummyService retrieved = container.get(DummyService.class);
        assertNotNull(retrieved);
        assertSame(service, retrieved);
    }

    @Test
    void testServiceNotFound() {
        assertThrows(ServiceNotFoundException.class, () -> container.get(DummyService.class));
    }

    @Test
    void testLifecycleExecution() throws Exception {
        DummyService service = new DummyService();
        container.register(DummyService.class, service);
        
        // Эмулируем запуск
        container.get(DummyService.class).onEnable();
        assertTrue(service.enabled);
    }

    @Test
    void testShutdownAll() {
        DummyService service1 = new DummyService();
        DummyService service2 = new DummyService();
        
        container.register(Service.class, service1); // Регистрируем под базовым интерфейсом
        container.register(DummyService.class, service2);

        container.shutdownAll();

        assertTrue(service1.disabled);
        assertTrue(service2.disabled);
        
        // После выключения контейнер должен быть пуст
        assertThrows(ServiceNotFoundException.class, () -> container.get(DummyService.class));
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                container.register(DummyService.class, new DummyService());
                container.get(DummyService.class);
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        
        assertNotNull(container.get(DummyService.class));
    }
}