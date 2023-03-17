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

package w.config.transformer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractTransformer<T> implements Transformer<T> {

    Class<T> type;

    protected abstract T doTransform(Object o);

    private T transform0(final Object o) {
        if (type.isAssignableFrom(o.getClass())) {
            return type.cast(o);
        }

        return doTransform(o);
    }


    @Override
    public T transformOrNull(final Object o) {
        return o == null ? null : transform0(o);
    }

    @Override
    public T transform(final Object o) {
        if (o == null) {
            return null;
        }

        val result = transform0(o);

        if (result == null) {
            throw new IllegalStateException("Cannot transform " + o + " to " + type.getSimpleName());
        }

        return result;
    }
}
