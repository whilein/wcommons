/*
 *    Copyright 2021 Whilein
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

package w.asm.patcher;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.util.Arrays;

/**
 * @author whilein
 */
@UtilityClass
public class Matchers {

    public @NotNull Matcher named(final @NotNull String name) {
        return info -> info.getName().equals(name);
    }

    public @NotNull Matcher returns(final @NotNull Class<?> returnType) {
        val type = Type.getType(returnType);

        return info -> info.getReturnType().equals(type);
    }

    public @NotNull Matcher takesArguments(final int count) {
        return info -> info.getArguments().length == count;
    }

    public @NotNull Matcher takesArguments(final @NotNull Class<?> @NotNull ... parameters) {
        val parameterTypes = Arrays.stream(parameters)
                .map(Type::getType)
                .toArray(Type[]::new);

        return info -> Arrays.equals(parameterTypes, info.getArguments());
    }

}
