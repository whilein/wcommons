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

package w.asm;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import w.util.Root;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 * @author whilein
 */
@UtilityClass
public class Asm {

    /**
     * Тип, наследующий {@code MagicAccessorImpl}, который можно использовать
     * в качестве супер-класса.
     */
    public final @NotNull Class<?> MAGIC_ACCESSOR_BRIDGE;

    /**
     * Имя типа, наследующего {@code MagicAccessorImpl}, который можно использовать
     * в качестве супер-класса.
     */
    public final @NotNull String MAGIC_ACCESSOR_BRIDGE_NAME = "w/asm/MagicAccessorImpl";

    static {
        val magicAccessorImpl = Root.MAGIC_ACCESSOR_IMPL_TYPE;

        val magicAccessorImplName = Type.getInternalName(magicAccessorImpl);

        val bridgeCode = createBridgeClass(magicAccessorImplName);

        MAGIC_ACCESSOR_BRIDGE = Root.defineClass(MAGIC_ACCESSOR_BRIDGE_NAME.replace('.', '/'),
                bridgeCode, 0, bridgeCode.length, null, null);
    }

    private byte[] createBridgeClass(final String magicAccessorImplName) {
        val cw = new ClassWriter(0);

        cw.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT, MAGIC_ACCESSOR_BRIDGE_NAME, null, magicAccessorImplName,
                null);

        {
            val constructor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null,
                    null);

            constructor.visitCode();

            // super call
            constructor.visitVarInsn(ALOAD, 0);
            constructor.visitMethodInsn(INVOKESPECIAL, magicAccessorImplName,
                    "<init>", "()V", false);
            // return
            constructor.visitInsn(RETURN);

            constructor.visitMaxs(1, 1);
            constructor.visitEnd();
        }

        cw.visitEnd();

        return cw.toByteArray();
    }
}
