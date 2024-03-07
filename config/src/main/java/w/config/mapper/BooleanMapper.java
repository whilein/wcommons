/*
 *    Copyright 2024 Whilein
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

import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
public final class BooleanMapper extends AbstractMapper<Boolean> {

    private static final AbstractMapper<Boolean> INSTANCE = new BooleanMapper();

    public BooleanMapper() {
        super(Boolean.class);
    }

    public static @NotNull AbstractMapper<Boolean> booleanMapper() {
        return INSTANCE;
    }

    @Override
    protected Boolean doMap(final Object o) {
        if (o instanceof String s) {
            return Boolean.valueOf(s);
        }

        if (o instanceof Number n) {
            return n.intValue() == 1;
        }

        return null;
    }

}
