/*
 *    Copyright 2023 Whilein
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package w.eventbus;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author whilein
 */
public interface EventBus {

    /**
     * Получить логгер, который будет использоваться для лога ошибок.
     *
     * @return Логгер
     */
    @NotNull Logger getLogger();

    /**
     * Удалить все подписки на события, зарегистрированные
     * из определённого объекта.
     *
     * @param owner Объект, в котором находятся подписки на события
     */
    void unregisterAll(@NotNull Object owner);

    /**
     * Удалить все подписки на события, зарегистрированные
     * из определённого класса.
     *
     * @param ownerType Класс, в котором находятся подписки на события
     */
    void unregisterAll(@NotNull Class<?> ownerType);

    /**
     * Удалить все подписки на события, зарегистрированные в определённом
     * неймспейсе.
     *
     * @param namespace Неймспейс
     */
    void unregisterAllByNamespace(@NotNull Object namespace);

    void unregisterAll();

    /**
     * Удалить подписку на события
     *
     * @param subscription Подписка
     */
    void unregister(@NotNull RegisteredSubscription subscription);

    /**
     * Зарегистрировать подписку на событие.
     *
     * @param namespace    Неймспейс
     * @param type         Класс события
     * @param subscription Слушатель
     * @param <E>          Тип события
     * @return Зарегистрированный слушатель
     */
    <E extends Event> @NotNull RegisteredSubscription register(
            @NotNull Object namespace,
            @NotNull Class<E> type,
            @NotNull Consumer<@NotNull E> subscription
    );

    /**
     * Зарегистрировать подписку на событие.
     *
     * @param type         Класс события
     * @param subscription Слушатель
     * @param <E>          Тип события
     * @return Зарегистрированный слушатель
     */
    <E extends Event> @NotNull RegisteredSubscription register(
            @NotNull Class<E> type,
            @NotNull Consumer<@NotNull E> subscription
    );

    /**
     * Зарегистрировать подписку на событие.
     *
     * @param namespace    Неймспейс
     * @param type         Класс события
     * @param subscription Подписка
     * @param order        Порядок слушателя
     * @param <E>          Тип события
     * @return Зарегистрированный слушатель
     */
    <E extends Event> @NotNull RegisteredSubscription register(
            @NotNull Object namespace,
            @NotNull Class<E> type,
            @NotNull PostOrder order,
            @NotNull Consumer<@NotNull E> subscription
    );

    /**
     * Зарегистрировать подписку на событие.
     *
     * @param type         Класс события
     * @param subscription Подписка
     * @param order        Порядок слушателя
     * @param <E>          Тип события
     * @return Зарегистрированный слушатель
     */
    <E extends Event> @NotNull RegisteredSubscription register(
            @NotNull Class<E> type,
            @NotNull PostOrder order,
            @NotNull Consumer<@NotNull E> subscription
    );

    /**
     * Зарегистрировать слушатели из объекта.
     *
     * @param namespace    Неймспейс
     * @param subscription Объект со слушателями
     */
    void register(@NotNull Object namespace, @NotNull Object subscription);

    /**
     * Зарегистрировать слушатели из класса. Методы, которые слушают события,
     * должны быть статичны.
     *
     * @param namespace        Неймспейс
     * @param subscriptionType Класс со статичными слушателями
     */
    void register(@NotNull Object namespace, @NotNull Class<?> subscriptionType);

    /**
     * Зарегистрировать слушатели из объекта.
     *
     * @param subscription Объект со слушателями
     */
    void register(@NotNull Object subscription);

    /**
     * Зарегистрировать слушатели из класса. Методы, которые слушают события,
     * должны быть статичны.
     *
     * @param subscriptionType Класс со статичными слушателями
     */
    void register(@NotNull Class<?> subscriptionType);

    <E extends AsyncEvent> @NotNull CompletableFuture<E> dispatchAsync(@NotNull E event);

    /**
     * Отправить событие на все слушатели, которые подписаны на него.
     *
     * @param event Событие
     */
    void dispatch(@NotNull Event event);

}
