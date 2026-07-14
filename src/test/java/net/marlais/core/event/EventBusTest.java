package net.marlais.core.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new AsynchronousEventBus();
    }

    // Тестовое событие
    static class TestEvent extends Event {
        final String message;

        TestEvent(String message) {
            super(false);
            this.message = message;
        }
    }

    // Тестовый слушатель
    static class TestListener {
        final List<String> callOrder = new ArrayList<>();

        @EventHandler(priority = EventPriority.HIGH)
        public void onTestHigh(TestEvent event) {
            callOrder.add("HIGH");
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onTestLow(TestEvent event) {
            callOrder.add("LOW");
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onTestNormal(TestEvent event) {
            callOrder.add("NORMAL");
        }
    }

    @Test
    void testEventExecutionAndPriorityOrder() {
        TestListener listener = new TestListener();
        eventBus.registerListeners(listener);

        TestEvent event = new TestEvent("Hello EventBus");
        eventBus.callEvent(event);

        // Проверяем, что событие дошло до всех обработчиков
        assertEquals(3, listener.callOrder.size());

        // Проверяем приоритетность (LOW -> NORMAL -> HIGH согласно .ordinal() перечисления)
        assertEquals("LOW", listener.callOrder.get(0));
        assertEquals("NORMAL", listener.callOrder.get(1));
        assertEquals("HIGH", listener.callOrder.get(2));
    }

    @Test
    void testUnregisterListener() {
        TestListener listener = new TestListener();
        eventBus.registerListeners(listener);
        eventBus.unregisterListeners(listener);

        TestEvent event = new TestEvent("Hello");
        eventBus.callEvent(event);

        // После отмены регистрации список вызовов должен остаться пустым
        assertTrue(listener.callOrder.isEmpty());
    }
}