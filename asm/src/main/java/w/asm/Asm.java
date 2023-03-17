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

package w.asm;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

/**
 * @author whilein
 */
@UtilityClass
public class Asm {

    public final @NotNull String
            OBJECT = "Ljava/lang/Object;",
            VOID = "V",
            BOOLEAN = "Z",
            BYTE = "B",
            SHORT = "S",
            INT = "I",
            LONG = "J",
            FLOAT = "F",
            DOUBLE = "D";

    public final @NotNull String
            OBJECT_TYPE = "java/lang/Object",
            EXCEPTION_TYPE = "java/lang/Exception";

    public @NotNull String methodDescriptor(
            final @NotNull Class<?> returnType,
            final @NotNull Class<?> @NotNull ... parameters
    ) {
        val result = new StringBuilder();
        result.append('(');

        for (val parameter : parameters) {
            result.append(Type.getDescriptor(parameter));
        }

        return result.append(')').append(Type.getDescriptor(returnType)).toString();
    }

    public @NotNull String methodDescriptor(
            final @NotNull String returnType,
            final @NotNull String @NotNull ... parameters
    ) {
        val result = new StringBuilder();
        result.append('(');

        for (val parameter : parameters) {
            result.append(parameter);
        }

        return result.append(')').append(returnType).toString();
    }

}
