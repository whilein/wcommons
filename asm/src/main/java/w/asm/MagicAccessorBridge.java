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

package w.asm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import w.unsafe.Unsafe;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author whilein
 */
public interface MagicAccessorBridge {

    static @NotNull MagicAccessorBridge getInstance() {
        return Initializer.INSTANCE;
    }

    static boolean isMagicAccessorAvailable() {
        return Initializer.INSTANCE.isAvailable();
    }

    boolean isAvailable();

    @NotNull Class<?> getType();

    @NotNull String getInternalName();

    @UtilityClass
    final class Initializer {

        private final MagicAccessorBridge INSTANCE;

        private final String MAGIC_ACCESSOR_BRIDGE_NAME = "w/asm/MagicAccessorImpl";

        static {
            Class<?> magicAccessorImpl;

            try {
                magicAccessorImpl = Class.forName("jdk.internal.reflect.MagicAccessorImpl");
            } catch (final ClassNotFoundException cfe) {
                try {
                    magicAccessorImpl = Class.forName("sun.reflect.MagicAccessorImpl");
                } catch (final ClassNotFoundException cfe1) {
                    magicAccessorImpl = null;
                }
            }

            if (magicAccessorImpl == null || !Unsafe.isUnsafeAvailable()) {
                INSTANCE = new StubImpl();
            } else {
                val magicAccessorImplName = Type.getInternalName(magicAccessorImpl);

                val bridgeCode = createBridgeClass(magicAccessorImplName);

                val magicAccessorBridge = Unsafe.getUnsafe().defineClass(
                        MAGIC_ACCESSOR_BRIDGE_NAME.replace('.', '/'),
                        bridgeCode, 0, bridgeCode.length, null, null
                );

                INSTANCE = new DefaultImpl(magicAccessorBridge, MAGIC_ACCESSOR_BRIDGE_NAME);
            }
        }

        private byte[] createBridgeClass(
                final String magicAccessorImplName
        ) {
            val cw = new ClassMaker(0);

            cw.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT, MAGIC_ACCESSOR_BRIDGE_NAME, null, magicAccessorImplName,
                    null);
            cw.visitEmptyConstructor(ACC_PUBLIC).visitEnd();
            cw.visitEnd();

            return cw.toByteArray();
        }

        private static final class StubImpl implements MagicAccessorBridge {

            private UnsupportedOperationException constructException() {
                return new UnsupportedOperationException(
                        "MagicAccessorImpl is not found on current JVM version (" + System.getProperty("java.version") + ")."
                );
            }

            @Override
            public boolean isAvailable() {
                return false;
            }

            @Override
            public @NotNull Class<?> getType() {
                throw constructException();
            }

            @Override
            public @NotNull String getInternalName() {
                throw constructException();
            }
        }

        @Getter
        @FieldDefaults(makeFinal = true)
        @RequiredArgsConstructor
        private static final class DefaultImpl implements MagicAccessorBridge {

            Class<?> type;
            String internalName;

            @Override
            public boolean isAvailable() {
                return true;
            }
        }
    }

}
