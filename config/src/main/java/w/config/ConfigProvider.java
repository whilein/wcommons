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

package w.config;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;

/**
 * @author whilein
 */
public interface ConfigProvider {

    <E> @NotNull E load(@NotNull File file, @NotNull Class<E> type);

    <E> @NotNull E load(@NotNull Path path, @NotNull Class<E> type);

    <E> @NotNull E load(@NotNull Reader reader, @NotNull Class<E> type);

    <E> @NotNull E load(@NotNull InputStream stream, @NotNull Class<E> type);

    <E> @NotNull E load(@NotNull String input, @NotNull Class<E> type);

    <E> @NotNull E load(byte @NotNull [] input, @NotNull Class<E> type);

    <E> @NotNull E load(@NotNull Object input, @NotNull Class<E> type);

    void save(@NotNull File file, @NotNull Object object);

    void save(@NotNull Path path, @NotNull Object object);

    void save(@NotNull Writer writer, @NotNull Object object);

    void save(@NotNull OutputStream stream, @NotNull Object object);

    @NotNull String saveAsString(@NotNull Object object);

    byte @NotNull [] saveAsBytes(@NotNull Object object);

    @NotNull Config newObject();

    @NotNull Config parse(@NotNull File file);

    @NotNull Config parse(@NotNull Path path);

    @NotNull Config parse(@NotNull Reader reader);

    @NotNull Config parse(@NotNull InputStream stream);

    @NotNull Config parse(@NotNull String input);

    @NotNull Config parse(byte @NotNull [] input);

}
