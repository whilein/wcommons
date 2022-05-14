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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/**
 * @author whilein
 */
public interface RegisteredSubscription extends Comparable<RegisteredSubscription> {

    /**
     * Получить объект, которому принадлежит обработчик.
     *
     * @return Объект, если обработчик это статичный метод, то {@code null}
     */
    @Nullable Object getOwner();

    /**
     * Получить класс, которому принадлежит обработчик.
     *
     * @return Класс
     */
    @NotNull Class<?> getOwnerType();

    /**
     * Получить порядок выполнения.
     *
     * @return Порядок выполнения
     */
    @NotNull PostOrder getPostOrder();

    /**
     * Получить врайтер байткода.
     *
     * @return Врайтер байткода.
     */
    @NotNull AsmDispatchWriter getDispatchWriter();

    /**
     * Получить неймспейс.
     *
     * @return Неймспейс
     */
    @NotNull Object getNamespace();

    /**
     * Получить статус игнорирования отменённых событий.
     *
     * @return Статус игнорирования отменённых событий
     */
    boolean isIgnoreCancelled();

    /**
     * Получить типы событий, на которые действует данная подписка.
     *
     * @return Типы событий
     */
    @Unmodifiable @NotNull Set<@NotNull Class<? extends Event>> getEvents();

}
