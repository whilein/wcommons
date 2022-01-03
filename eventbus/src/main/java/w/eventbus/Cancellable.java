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

/**
 * @author whilein
 */
public interface Cancellable {

    /**
     * Получить, отменено ли событие.
     *
     * @return {@code true}, если событие отменено
     */
    boolean isCancelled();

    /**
     * Изменить статус отмены события.
     *
     * @param cancelled Новое значение отмены события.
     */
    void setCancelled(boolean cancelled);

}
