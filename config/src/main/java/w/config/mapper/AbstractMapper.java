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

package w.config.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.Nullable;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractMapper<T> implements Mapper<T> {

    Class<T> type;

    protected abstract T doMap(Object o);

    private T map0(final Object o) {
        if (type.isAssignableFrom(o.getClass())) {
            return type.cast(o);
        }

        return doMap(o);
    }

    @Override
    public final @Nullable T map(@Nullable Object o) {
        return o == null ? null : map0(o);
    }

    @Override
    public final @Nullable T mapStrict(@Nullable Object o) {
        if (o == null) return null;

        val result = map0(o);

        if (result == null) {
            throw new IllegalStateException("Cannot map " + o + " to " + type.getSimpleName());
        }

        return result;
    }
}
