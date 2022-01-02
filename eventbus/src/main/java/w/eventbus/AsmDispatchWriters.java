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

package w.eventbus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * @author whilein
 */
@UtilityClass
public class AsmDispatchWriters {

    public static @NotNull AsmDispatchWriter fromConsumer(final @NotNull Consumer<?> consumer) {
        return new ConsumerWriter(consumer);
    }

    public static @NotNull AsmDispatchWriter fromMethod(final @Nullable Object handle, final @NotNull Method method) {
        return new MethodWriter(
                handle,
                method.getDeclaringClass(),
                Type.getType(method.getDeclaringClass()),
                Type.getInternalName(method.getParameterTypes()[0]),
                method.getName(),
                Type.getMethodDescriptor(method),
                handle == null ? INVOKESTATIC : INVOKESPECIAL
        );
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MethodWriter implements AsmDispatchWriter {

        @Getter
        Object handle;

        @Getter
        Class<?> handleType;

        @Getter
        Type type;

        String eventType;

        String methodName;
        String methodDescriptor;

        int opcode;

        @Override
        public @NotNull String getName() {
            return handleType.getName() + " " + methodName + "(" + eventType.replace('/', '.') + ")";
        }

        @Override
        public void write(final @NotNull MethodVisitor mv, final @NotNull String name, final @Nullable String field) {
            mv.visitVarInsn(ALOAD, 1);

            mv.visitMethodInsn(opcode, type.getInternalName(), methodName,
                    methodDescriptor, false);
        }
    }


    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ConsumerWriter implements AsmDispatchWriter {

        private static final Type TYPE = Type.getType(Consumer.class);

        Consumer<?> handle;

        @Override
        public @Nullable Object getHandle() {
            return handle;
        }

        @Override
        public @NotNull Class<?> getHandleType() {
            return Consumer.class;
        }

        @Override
        public @NotNull Type getType() {
            return TYPE;
        }

        @Override
        public @NotNull String getName() {
            return handle.toString();
        }

        @Override
        public void write(final @NotNull MethodVisitor mv, final @NotNull String name, final @Nullable String field) {
            mv.visitVarInsn(ALOAD, 1);

            mv.visitMethodInsn(INVOKEINTERFACE, TYPE.getInternalName(), "accept",
                    "(Ljava/lang/Object;)V", true);
        }
    }

}
