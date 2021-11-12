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

package w.sql.orm.type;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author whilein
 */
@UtilityClass
public class TypeRegistry {

    private final Map<Class<?>, TypeTransformer> TRANSFORMERS
            = new HashMap<>();

    static {
        registerPrimitive(boolean.class, Boolean.class, "getBoolean");
        registerPrimitive(byte.class, Byte.class, "getByte");
        registerPrimitive(short.class, Short.class, "getShort");
        registerPrimitive(int.class, Integer.class, "getInt");
        registerPrimitive(long.class, Long.class, "getLong");
        registerPrimitive(float.class, Float.class, "getFloat");
        registerPrimitive(double.class, Double.class, "getDouble");
        registerTransformer(new Common(String.class, "getString"));
        // todo date and time, bytes
    }

    private static void registerPrimitive(final Class<?> type, final Class<?> wrapperType, final String name) {
        val primitive = new Common(type, name);
        registerTransformer(primitive);
        registerTransformer(new Wrapper(wrapperType, primitive));
    }

    private static void registerTransformer(final TypeTransformer transformer) {
        TRANSFORMERS.put(transformer.getType(), transformer);
    }

    public boolean isCommonType(final @NotNull Class<?> type) {
        return TRANSFORMERS.containsKey(type);
    }

    public @NotNull TypeTransformer lookupTransformer(final @NotNull Class<?> type) {
        return TRANSFORMERS.get(type);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Wrapper implements TypeTransformer {
        @Getter
        Class<?> type;

        Common primitive;

        @Override
        public void write(final @NotNull MethodVisitor mv) {
            primitive.write(mv);

            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    Type.getInternalName(type),
                    "valueOf",
                    "(" + Type.getDescriptor(primitive.type) + ")" + Type.getDescriptor(type),
                    false
            );
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Common implements TypeTransformer {

        @Getter
        Class<?> type;
        String name;

        @Override
        public void write(final @NotNull MethodVisitor mv) {
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(ResultSet.class),
                    name, "(I)" + Type.getDescriptor(type), true);
        }
    }

}
