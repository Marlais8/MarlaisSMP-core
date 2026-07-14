package net.marlais.core.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Потокобезопасная реализация шины событий с поддержкой приоритетов.
 */
public class AsynchronousEventBus implements EventBus {

    // Карта, связывающая класс События со списком зарегистрированных оберток-слушателей
    private final Map<Class<? extends Event>, List<RegisteredListener>> bindings = new ConcurrentHashMap<>();

    @Override
    public void onEnable() throws Exception {
        // Инициализация службы шины событий
    }

    @Override
    public void onDisable() throws Exception {
        bindings.clear();
    }

    @Override
    public void registerListeners(Object listener) {
        Method[] methods = listener.getClass().getDeclaredMethods();

        for (Method method : methods) {
            // Ищем методы с аннотацией @EventHandler
            if (!method.isAnnotationPresent(EventHandler.class)) {
                continue;
            }

            // Проверяем, что у метода ровно 1 параметр и он наследуется от Event
            if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                System.err.println("Ошибка регистрации: Метод " + method.getName() + " должен иметь ровно один аргумент типа Event");
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            
            method.setAccessible(true); // Разрешаем вызов приватных методов

            RegisteredListener registeredListener = new RegisteredListener(listener, method, annotation.priority());
            
            bindings.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(registeredListener);
        }

        // Сортируем слушателей по приоритету после каждой регистрации
        for (List<RegisteredListener> listeners : bindings.values()) {
            listeners.sort(Comparator.comparingInt(l -> l.priority.ordinal()));
        }
    }

    @Override
    public void unregisterListeners(Object listener) {
        for (List<RegisteredListener> listeners : bindings.values()) {
            listeners.removeIf(registeredListener -> registeredListener.instance == listener);
        }
    }

    @Override
    public void callEvent(Event event) {
        List<RegisteredListener> listeners = bindings.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) {
            return;
        }

        for (RegisteredListener listener : listeners) {
            try {
                listener.method.invoke(listener.instance, event);
            } catch (Exception e) {
                System.err.println("Ошибка при передаче события " + event.getClass().getSimpleName() + " в " + listener.instance.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }

    // Вспомогательный класс для хранения данных слушателя
    private static class RegisteredListener {
        final Object instance;
        final Method method;
        final EventPriority priority;

        RegisteredListener(Object instance, Method method, EventPriority priority) {
            this.instance = instance;
            this.method = method;
            this.priority = priority;
        }
    }
}