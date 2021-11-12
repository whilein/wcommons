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

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.asm.Asm;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @author whilein
 */
@UtilityClass
public final class AgentInstaller {

    private final long PID;
    private final String JAVA_EXECUTABLE;

    private final Class<?> AGENT_MAIN;
    private final Field AGENT_INSTRUMENTATION;

    private volatile Instrumentation instrumentation;

    static {
        val process = ProcessHandle.current();

        PID = process.pid();

        JAVA_EXECUTABLE = process.info().command()
                .orElseGet(() -> System.getProperty("JAVA_HOME") // or default
                        + "/bin/java");

        try {
            AGENT_MAIN = ClassLoader.getSystemClassLoader().loadClass("w.asm.patcher.AgentMain");

            AGENT_INSTRUMENTATION = AGENT_MAIN.getDeclaredField("instrumentation");
            AGENT_INSTRUMENTATION.setAccessible(true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Создать Jar-ник с одним классом и манифестом,
     * для того, чтобы запустить его через {@link #runAgent(Path)}
     *
     * @return путь нового Jar-ника
     * @throws IOException ошибка при создании временного файла
     */
    private Path createAgentJar() throws IOException {
        val temporary = Files.createTempFile("wcommons", ".jar");

        val agentMainName = AGENT_MAIN.getName();

        val manifest = new Manifest();
        val manifestAttributes = manifest.getMainAttributes();
        manifestAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifestAttributes.put(new Attributes.Name("Can-Redefine-Classes"), "true");
        manifestAttributes.put(new Attributes.Name("Can-Retransform-Classes"), "true");
        manifestAttributes.put(new Attributes.Name("Agent-Class"), agentMainName);
        manifestAttributes.put(new Attributes.Name("Main-Class"), agentMainName);

        try (val jar = new JarOutputStream(Files.newOutputStream(temporary), manifest)) {
            val mainClass = new ZipEntry(agentMainName.replace('.', '/') + ".class");

            jar.putNextEntry(mainClass);
            jar.write(Asm.toByteArray(AGENT_MAIN));
        }

        return temporary;
    }

    /**
     * Запустить Jar, ибо нельзя подключать агент в той же JVM, начиная с Java 9
     *
     * @param agent Путь до Jar агента
     * @throws IOException            ошибка, если не удалось запустить процесс
     * @throws InterruptedException   ошибка, если не удалось дождаться завершения процесса
     * @throws IllegalAccessException ошибка, если по какой-то причине не
     *                                вызвался {@link Field#setAccessible(boolean)} у {@link AgentMain}
     */
    private Instrumentation runAgent(final Path agent) throws IOException, InterruptedException, IllegalAccessException {
        val cp = AgentInstaller.class.getProtectionDomain().getCodeSource()
                .getLocation();

        val args = new String[]{
                JAVA_EXECUTABLE,
                "-cp",
                cp.getFile(),
                "-jar",
                agent.toAbsolutePath().toString(),
                String.valueOf(PID)
        };

        val process = new ProcessBuilder(args).start();

        try (val out = process.getInputStream()) {
            out.transferTo(System.out);
        }

        try (val err = process.getErrorStream()) {
            err.transferTo(System.err);
        }

        process.waitFor();

        return (Instrumentation) AGENT_INSTRUMENTATION.get(null);
    }

    @SneakyThrows
    public @NotNull Instrumentation getInstrumentation() {
        if (instrumentation != null) {
            synchronized (AgentInstaller.class) {
                if (instrumentation != null) {
                    return instrumentation;
                }
            }
        }

        val agentJar = createAgentJar();

        val instrumentation = runAgent(agentJar);

        if (instrumentation == null) {
            throw new IllegalStateException("Unable to install java agent");
        }

        return AgentInstaller.instrumentation = instrumentation;
    }

}
