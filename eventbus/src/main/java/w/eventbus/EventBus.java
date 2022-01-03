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
import org.slf4j.Logger;

/**
 * @author whilein
 */
public interface EventBus<T extends SubscribeNamespace> extends EventBusRegisterer<T> {

    /**
     * Получить логгер, который будет использоваться для лога ошибок.
     *
     * @return Логгер
     */
    @NotNull Logger getLogger();

    /**
     * Отправить событие на все слушатели, которые подписаны на него.
     *
     * @param event Событие
     */
    void dispatch(@NotNull Event event);

    /**
     * Отправить событие на все слушатели, которые подписаны на него.
     * Если произойдёт ошибка в одном из слушателей, то выполнение оборвётся на нём.
     *
     * @param event Событие
     */
    void unsafeDispatch(@NotNull Event event);

    /**
     * Применить все изменения с удалением/добавлением слушателей.
     */
    void bake();

    // <E> @NotNull EventDispatcher<E> getDispatcher(@NotNull Class<E> eventType);

}
