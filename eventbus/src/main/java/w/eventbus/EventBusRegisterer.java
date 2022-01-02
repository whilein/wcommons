/*
 *    Copyright 2022 Whilein
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

import java.util.function.Consumer;

/**
 * @author whilein
 */
public interface EventBusRegisterer<T extends SubscribeNamespace> {

    /**
     * Удалить все подписки на события, зарегистрированные
     * из определённого объекта.
     *
     * @param holder Объект, в котором находятся слушатели событий.
     */
    void unregisterAll(@NotNull Object holder);

    void unregisterAll(@NotNull Class<?> holderType);

    /**
     * Удалить все подписки на события, зарегистрированные в определённом
     * неймспейсе.
     *
     * @param namespace Неймспейс
     */
    void unregisterAll(@NotNull T namespace);

    void unregister(@NotNull RegisteredEventSubscription<?> subscription);

    /**
     * Зарегистрировать слушатель.
     *
     * @param namespace    Неймспейс
     * @param type         Класс события
     * @param subscription Слушатель
     * @param <E>          Тип события
     * @return Зарегистрированный слушатель
     */
    <E extends Event> @NotNull RegisteredEventSubscription<E> register(
            @NotNull T namespace,
            @NotNull Class<E> type,
            @NotNull Consumer<@NotNull E> subscription
    );

    /**
     * Зарегистрировать слушатель.
     *
     * @param namespace    Неймспейс
     * @param type         Класс события
     * @param subscription Подписка
     * @param order        Порядок слушателя
     * @param <E>          Тип события
     * @return Зарегистрированный слушатель
     */
    <E extends Event> @NotNull RegisteredEventSubscription<E> register(
            @NotNull T namespace,
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
    void register(@NotNull T namespace, @NotNull Object subscription);

    void register(@NotNull T namespace, @NotNull Class<?> subscriptionType);
}
