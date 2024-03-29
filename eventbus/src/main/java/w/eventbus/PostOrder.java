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

/**
 * @author whilein
 */
public enum PostOrder {

    /**
     * Выполняется раньше всех
     */
    LOWEST,

    /**
     * Выполняется после {@link #LOWEST}
     */
    LOW,

    /**
     * Выполняется после {@link #LOW}, используется по умолчанию
     */
    NORMAL,

    /**
     * Выполняется после {@link #NORMAL}
     */
    HIGH,

    /**
     * Выполняется позже всех
     */
    HIGHEST,

    /**
     * Выполняется для отслеживания финальных состояний событий, не рекомендуется
     * изменять событие в данном приоритете, поскольку
     */
    MONITOR

}
