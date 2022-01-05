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

package w.asm.patcher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import w.agent.AgentInstrumentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author whilein
 */
@UtilityClass
public class AsmPatcher {

    public @NotNull AsmRedefine redefine(
            final @NotNull Class<?> type
    ) {
        return new AsmRedefineImpl(
                type,
                new ArrayList<>()
        );
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Interceptor {
        Matcher matcher;
        Function<MethodVisitor, MethodVisitor> mv;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class AsmRedefineMethodImpl implements AsmRedefineMethod {

        Matcher matcher;
        AsmRedefineImpl redefine;

        @Override
        public @NotNull AsmRedefine intercept(
                final @NotNull Function<@NotNull MethodVisitor, @NotNull MethodVisitor> mv
        ) {
            redefine.interceptors.add(new Interceptor(matcher, mv));

            return redefine;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class AsmRedefineImpl implements AsmRedefine {

        Class<?> type;
        List<Interceptor> interceptors;

        @Override
        public @NotNull AsmRedefineMethod on(final @NotNull Matcher matcher) {
            return new AsmRedefineMethodImpl(matcher, this);
        }

        @Override
        public void apply(final @NotNull File file) {
            _apply(file);
        }

        @Override
        public void apply() {
            _apply(null);
        }

        @SneakyThrows
        private void _apply(final File file) {
            if (interceptors.isEmpty()) {
                return;
            }

            val internalType = Type.getInternalName(type);

            AgentInstrumentation.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(
                        final ClassLoader loader,
                        final String className,
                        final Class<?> classBeingRedefined,
                        final ProtectionDomain protectionDomain,
                        final byte[] buffer
                ) throws IllegalClassFormatException {
                    if (className.equals(internalType)) {
                        try {
                            val reader = new ClassReader(buffer);

                            val writer = new ClassWriter(
                                    reader,
                                    ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS
                            );

                            reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
                                @Override
                                public MethodVisitor visitMethod(
                                        final int access,
                                        final String name,
                                        final String descriptor,
                                        final String signature,
                                        final String[] exceptions
                                ) {
                                    MethodVisitor mv = super.visitMethod(
                                            access,
                                            name,
                                            descriptor,
                                            signature,
                                            exceptions
                                    );

                                    val methodInfo = ImmutableMethodInfo.from(name, descriptor);

                                    for (val interceptor : interceptors) {
                                        if (interceptor.matcher.matches(methodInfo)) {
                                            mv = interceptor.mv.apply(mv);
                                        }
                                    }

                                    return mv;
                                }
                            }, 0);

                            val bytes = writer.toByteArray();

                            if (file != null) {
                                val classFile = new File(file, className + ".class");
                                val packageDir = classFile.getParentFile();

                                if (!packageDir.exists() && !packageDir.mkdirs()) {
                                    throw new IOException("Cannot make dir for " + classFile);
                                }

                                try (val os = new FileOutputStream(classFile)) {
                                    os.write(bytes);
                                }
                            }

                            return bytes;
                        } catch (final Exception e) {
                            e.printStackTrace();
                        } finally {
                            AgentInstrumentation.removeTransformer(this);
                        }
                    }

                    return ClassFileTransformer.super.transform(loader, className,
                            classBeingRedefined, protectionDomain, buffer);
                }
            }, true);

            AgentInstrumentation.retransformClasses(type);
        }
    }

}
