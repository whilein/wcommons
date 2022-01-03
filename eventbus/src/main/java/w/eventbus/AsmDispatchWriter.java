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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author whilein
 */
public interface AsmDispatchWriter {

    /**
     * Получить тип объекта, которому принадлежит метод.
     *
     * @return Тип объекта (asm)
     */
    @NotNull Type getOwnerType();

    /**
     * Получить имя метода.
     *
     * @return Имя метода
     */
    @NotNull String getName();

    /**
     * Записать выполнение метод в {@code mv}
     *
     * @param mv Метод визитор, в который будет записано выполнение метода
     */
    void write(@NotNull MethodVisitor mv);

}
