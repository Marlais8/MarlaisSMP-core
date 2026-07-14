# 04_EVENT_BUS.md

## 1. Назначение документа
Этот документ описывает архитектуру и API шины событий (Event Bus) MarlaisSMP Core. Шина событий обеспечивает обмен сообщениями и событиями между ядром и независимыми модулями.

## 2. Архитектурные требования
- **Паттерн Издатель-Подписчик (Publisher-Subscriber):** Модули могут публиковать события (Publish) и подписываться на них (Subscribe).
- **Слушатели через аннотации:** Для удобства разработчиков подписка на события должна осуществляться с помощью аннотации `@EventHandler` над методами классов-слушателей.
- **Приоритеты выполнения:** События должны поддерживать приоритеты (LOW, NORMAL, HIGH), чтобы некоторые плагины могли обрабатывать или отменять события раньше других.

## 3. Ключевые компоненты API

### 3.1. Базовый класс события (Event)
Каждое событие в системе должно наследоваться от этого абстрактного класса.

```java
package net.marlais.core.event;

/**
 * Базовый класс для всех событий ядра.
 */
public abstract class Event {
    private final boolean async;

    protected Event(boolean async) {
        this.async = async;
    }

    public boolean isAsync() {
        return async;
    }
}
```

### 3.2. Аннотация @EventHandler
Помечает методы, которые хотят получать события.

```java
package net.marlais.core.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    EventPriority priority() default EventPriority.NORMAL;
}
```

### 3.3. Приоритеты событий (EventPriority)
```java
package net.marlais.core.event;

public enum EventPriority {
    LOW,
    NORMAL,
    HIGH
}
```

### 3.4. Интерфейс EventBus (Шина событий)
Служба регистрируется в контейнере под интерфейсом `EventBus`.

```java
package net.marlais.core.event;

import net.marlais.core.service.Service;

public interface EventBus extends Service {
    /**
     * Регистрирует все методы с аннотацией @EventHandler в указанном объекте-слушателе.
     */
    void registerListeners(Object listener);

    /**
     * Отменяет регистрацию всех слушателей для данного объекта.
     */
    void unregisterListeners(Object listener);

    /**
     * Публикует событие, передавая его всем зарегистрированным слушателям.
     */
    void callEvent(Event event);
}
```