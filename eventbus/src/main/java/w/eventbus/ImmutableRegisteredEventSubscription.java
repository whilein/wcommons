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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author whilein
 */
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImmutableRegisteredEventSubscription<T extends Event> implements RegisteredEventSubscription<T> {
    AsmDispatchWriter dispatchWriter;

    Object owner;

    Class<?> ownerType;

    PostOrder postOrder;

    boolean ignoreCancelled;

    SubscribeNamespace namespace;

    Class<T> event;

    /**
     * Создать иммутабельную подписку на событие {@code event}
     *
     * @param dispatchWriter  Врайтер
     * @param owner           Владелец
     * @param ownerType       Класс владельца
     * @param postOrder       Порядок выполнения
     * @param ignoreCancelled Игнорировать отменённые события
     * @param namespace       Неймспейс
     * @param event           Класс события
     * @param <T>             Тип события
     * @return Новая иммутабельная побписка на событие
     */
    public static @NotNull <T extends Event> RegisteredEventSubscription<T> create(
            final @NotNull AsmDispatchWriter dispatchWriter,
            final @Nullable Object owner,
            final @NotNull Class<?> ownerType,
            final @NotNull PostOrder postOrder,
            final boolean ignoreCancelled,
            final @NotNull SubscribeNamespace namespace,
            final @NotNull Class<T> event
    ) {
        return new ImmutableRegisteredEventSubscription<>(dispatchWriter, owner, ownerType,
                postOrder, ignoreCancelled, namespace, event);
    }

    @Override
    public int compareTo(final @NotNull RegisteredEventSubscription<?> o) {
        val compareOrder = postOrder.compareTo(o.getPostOrder());

        if (compareOrder != 0) {
            return compareOrder;
        }

        return Boolean.compare(ignoreCancelled, o.isIgnoreCancelled());
    }
}
