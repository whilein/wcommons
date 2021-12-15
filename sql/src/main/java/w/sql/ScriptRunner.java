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

package w.sql;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

public interface ScriptRunner {

    @NotNull ScriptRunner classLoader(@NotNull ClassLoader cl);
    @NotNull ScriptRunner schema(@NotNull String schema);

    @NotNull ScriptRunner run(@NotNull String resource);

    @NotNull ScriptRunner run(@NotNull File file);
    @NotNull ScriptRunner run(@NotNull Path path);

    @NotNull ScriptRunner run(@NotNull InputStream is);

    @NotNull ScriptRunner run(@NotNull Reader reader);

}
