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

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * @author whilein
 */
public class ClassMaker extends ClassVisitor {

    public ClassWriter cw;

    public ClassMaker(final ClassVisitor cv) {
        super(Opcodes.ASM9, cv);

        if (cv instanceof ClassWriter) {
            this.cw = (ClassWriter) cv;
        }
    }

    public ClassMaker(final int flags) {
        super(Opcodes.ASM9, new ClassWriter(flags));

        this.cw = (ClassWriter) super.cv;
    }

    public String name;
    public String superName;

    public byte[] toByteArray() {
        final ClassWriter cw;

        if ((cw = this.cw) == null) {
            throw new IllegalStateException("ClassMaker is not created from ClassWriter");
        }

        return cw.toByteArray();
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces
    ) {
        this.name = name;
        this.superName = superName;

        super.visit(version, access, name, signature, superName, interfaces);
    }

    public @NotNull MethodMaker visitEmptyConstructor(
            final int access,
            final @NotNull Class<?> superType
    ) {
        return visitEmptyConstructor(access, Type.getInternalName(superType));
    }

    public @NotNull MethodMaker visitEmptyConstructor(
            final int access,
            final @NotNull Type type
    ) {
        return visitEmptyConstructor(access, type.getInternalName());
    }

    public @NotNull MethodMaker visitEmptyConstructor(
            final int access
    ) {
        final String superName;

        if ((superName = this.superName) == null) {
            throw new IllegalStateException("Cannot #visitEmptyConstructor before #visit");
        }

        return visitEmptyConstructor(access, superName);
    }

    public @NotNull MethodMaker visitEmptyConstructor(
            final int access,
            final @NotNull String superName
    ) {
        val constructor = visitMethod(access, "<init>", "()V",
                null, null);

        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
        constructor.visitInsn(RETURN);

        constructor.visitMaxs(1, 1);

        constructor.visitEnd();

        return constructor;
    }

    @Override
    public MethodMaker visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions
    ) {
        return new MethodMaker(super.visitMethod(access, name, descriptor, signature, exceptions));
    }
}
