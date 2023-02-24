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

package w.agent;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @author whilein
 */
@UtilityClass
public final class AgentInstrumentation {

    private final Instrumentation INSTRUMENTATION;

    static {
        Instrumentation instrumentation;


        if (Boolean.getBoolean("wcommons.agent.premain")) {
            try {
                instrumentation = getInstrumentation(getAgentMain());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            val process = ProcessHandle.current();

            val pid = process.pid();

            val javaExecutable = process.info().command()
                    .orElseGet(() -> System.getProperty("JAVA_HOME") // or default
                            + "/bin/java");

            try {
                val agentJar = createAgentJar();

                instrumentation = runAgent(agentJar, javaExecutable, pid);

                if (instrumentation == null) {
                    throw new IllegalStateException("Unable to install java agent");
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        INSTRUMENTATION = instrumentation;
    }

    /**
     * Создать Jar-ник с одним классом и манифестом,
     * для того, чтобы запустить его через {@link #runAgent(Path, String, long)} (Path)}
     *
     * @return путь нового Jar-ника
     * @throws IOException ошибка при создании временного файла
     */
    private Path createAgentJar() throws IOException {
        val temporary = Files.createTempFile("wcommons", ".jar");

        val agentMainName = "w.agent.AgentMain";

        val manifest = new Manifest();
        val manifestAttributes = manifest.getMainAttributes();
        manifestAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifestAttributes.put(new Attributes.Name("Can-Redefine-Classes"), "true");
        manifestAttributes.put(new Attributes.Name("Can-Retransform-Classes"), "true");
        manifestAttributes.put(new Attributes.Name("Agent-Class"), agentMainName);
        manifestAttributes.put(new Attributes.Name("Main-Class"), agentMainName);

        try (val jar = new JarOutputStream(Files.newOutputStream(temporary), manifest)) {
            val mainClassName = agentMainName.replace('.', '/') + ".class";

            val mainClass = new ZipEntry(mainClassName);

            jar.putNextEntry(mainClass);

            try (val resource = AgentInstrumentation.class.getClassLoader()
                    .getResourceAsStream(mainClassName)) {
                if (resource == null) {
                    throw new IllegalStateException("Cannot find " + mainClassName
                            + " AgentInstrumentation class loader");
                }

                jar.write(resource.readAllBytes());
            }
        }

        return temporary;
    }

    /**
     * Запустить Jar, ибо нельзя подключать агент в той же JVM, начиная с Java 9
     *
     * @param agent Путь до Jar агента
     * @throws IOException          ошибка, если не удалось запустить процесс
     * @throws InterruptedException ошибка, если не удалось дождаться завершения процесса
     */
    private Instrumentation runAgent(final Path agent, final String javaExecutable, final long pid)
            throws IOException, InterruptedException {
        val cp = AgentInstrumentation.class.getProtectionDomain().getCodeSource()
                .getLocation();

        val args = new String[]{
                javaExecutable,
                "-cp",
                cp.getFile(),
                "-jar",
                agent.toAbsolutePath().toString(),
                String.valueOf(pid)
        };

        val process = new ProcessBuilder(args).start();

        try (val out = process.getInputStream()) {
            out.transferTo(System.out);
        }

        try (val err = process.getErrorStream()) {
            err.transferTo(System.err);
        }

        process.waitFor();

        try {
            return getInstrumentation(getAgentMain());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getAgentMain() throws ClassNotFoundException {
        return ClassLoader.getSystemClassLoader().loadClass("w.agent.AgentMain");
    }

    @SneakyThrows
    private Instrumentation getInstrumentation(Class<?> agentMain) {
        val field = agentMain.getDeclaredField("instrumentation");
        field.setAccessible(true);

        return (Instrumentation) field.get(null);
    }

    public void addTransformer(final ClassFileTransformer transformer, final boolean canRetransform) {
        INSTRUMENTATION.addTransformer(transformer, canRetransform);
    }

    public void addTransformer(final ClassFileTransformer transformer) {
        INSTRUMENTATION.addTransformer(transformer);
    }

    public boolean removeTransformer(final ClassFileTransformer transformer) {
        return INSTRUMENTATION.removeTransformer(transformer);
    }

    public boolean isRetransformClassesSupported() {
        return INSTRUMENTATION.isRetransformClassesSupported();
    }

    public void retransformClasses(final Class<?>... classes) throws UnmodifiableClassException {
        INSTRUMENTATION.retransformClasses(classes);
    }

    public boolean isRedefineClassesSupported() {
        return INSTRUMENTATION.isRedefineClassesSupported();
    }

    public void redefineClasses(final ClassDefinition... definitions)
            throws ClassNotFoundException, UnmodifiableClassException {
        INSTRUMENTATION.redefineClasses(definitions);
    }

    public boolean isModifiableClass(final Class<?> theClass) {
        return INSTRUMENTATION.isModifiableClass(theClass);
    }

    public Class<?>[] getAllLoadedClasses() {
        return INSTRUMENTATION.getAllLoadedClasses();
    }

    public Class<?>[] getInitiatedClasses(final ClassLoader loader) {
        return INSTRUMENTATION.getInitiatedClasses(loader);
    }

    public long getObjectSize(final Object objectToSize) {
        return INSTRUMENTATION.getObjectSize(objectToSize);
    }

    public void appendToBootstrapClassLoaderSearch(final JarFile jarfile) {
        INSTRUMENTATION.appendToBootstrapClassLoaderSearch(jarfile);
    }

    public void appendToSystemClassLoaderSearch(final JarFile jarfile) {
        INSTRUMENTATION.appendToSystemClassLoaderSearch(jarfile);
    }

    public boolean isNativeMethodPrefixSupported() {
        return INSTRUMENTATION.isNativeMethodPrefixSupported();
    }

    public void setNativeMethodPrefix(final ClassFileTransformer transformer, final String prefix) {
        INSTRUMENTATION.setNativeMethodPrefix(transformer, prefix);
    }

    public void redefineModule(
            final Module module,
            final Set<Module> extraReads,
            final Map<String, Set<Module>> extraExports,
            final Map<String, Set<Module>> extraOpens,
            final Set<Class<?>> extraUses,
            final Map<Class<?>, List<Class<?>>> extraProvides
    ) {
        INSTRUMENTATION.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    public boolean isModifiableModule(final Module module) {
        return INSTRUMENTATION.isModifiableModule(module);
    }
}
