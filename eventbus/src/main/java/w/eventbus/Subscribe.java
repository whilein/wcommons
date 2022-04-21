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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author whilein
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

    /**
     * Получить порядок выполнения.
     *
     * @return Порядок выполнения
     */
    @NotNull PostOrder order() default PostOrder.NORMAL;

    /**
     * Получить статус игнорирования отменённых событий.
     *
     * @return Статус игнорирования отменённых событий
     */
    boolean ignoreCancelled() default false;

    /**
     * Здесь можно указать типы событий, которые нужно слушать дополнительно.
     * <p>
     * Допустим метод принимает {@link Event}, здесь можно указать например {@link AsyncEvent} и тогда
     * {@link AsyncEvent} и {@link Event} будут отправляться в этот метод.
     *
     * @return Типы событий
     */
    @NotNull Class<?>[] types() default {};

}
