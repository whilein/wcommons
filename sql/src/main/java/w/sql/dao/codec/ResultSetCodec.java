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

package w.sql.dao.codec;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author whilein
 */
@UtilityClass
public class ResultSetCodec {

    private final Map<Class<?>, ResultSetReader<?>> READERS = new HashMap<>();

    private final ResultSetReader<?> DEFAULT_READER = ResultSet::getObject;

    static {
        addReader(String.class, ResultSet::getString);
        addReader(Boolean.class, ResultSet::getBoolean);
        addReader(Byte.class, ResultSet::getByte);
        addReader(Short.class, ResultSet::getShort);
        addReader(Integer.class, ResultSet::getInt);
        addReader(Long.class, ResultSet::getLong);
        addReader(Double.class, ResultSet::getDouble);
        addReader(Float.class, ResultSet::getFloat);
        addReader(LocalDateTime.class, (rs, column) -> rs.getTimestamp(column).toLocalDateTime());
        addReader(LocalDate.class, (rs, column) -> rs.getDate(column).toLocalDate());
    }

    private <T> void addReader(final Class<T> type, final ResultSetReader<T> reader) {
        READERS.put(type, reader);
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull ResultSetReader<T> findReader(final @NotNull Class<T> type) {
        return (ResultSetReader<T>) READERS.getOrDefault(type, DEFAULT_READER);
    }

}
