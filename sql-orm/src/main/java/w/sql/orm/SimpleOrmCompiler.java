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

package w.sql.orm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import w.commons.sql.Messenger;
import w.sql.orm.definition.DaoDefinition;

import java.lang.invoke.MethodHandles;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V11;

/**
 * @author whilein
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleOrmCompiler<T> implements OrmCompiler<T> {

    private static final String MESSENGER = Type.getInternalName(Messenger.class);

    OrmManager ormManager;

    DaoDefinition<T> definition;

    String name;
    String daoName;

    ClassWriter cw;

    public static <T> @NotNull OrmCompiler<T> create(
            final @NotNull OrmManager ormManager,
            final @NotNull DaoDefinition<T> definition
    ) {
        val daoName = Type.getInternalName(definition.getType());
        val name = daoName + "$Compiled";

        return new SimpleOrmCompiler<>(
                ormManager,
                definition,
                name,
                daoName,
                new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
        );
    }

    @Override
    @SneakyThrows
    public @NotNull T compile() {
        cw.visit(V11, ACC_PUBLIC | ACC_FINAL,
                name, null, "java/lang/Object", new String[]{daoName});

        initFields();
        initConstructor();

        val type = defineType(cw).asSubclass(definition.getType());

        val constructor = type.getDeclaredConstructor(Messenger.class);
        constructor.setAccessible(true);

        return constructor.newInstance(ormManager.getMessenger());
    }

    private void initFields() {
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "messenger",
                'L' + MESSENGER + ';', null, null).visitEnd();
    }

    private void initConstructor() {
        val mv = cw.visitMethod(ACC_PRIVATE, "<init>",
                "(L" + MESSENGER + ";)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, name, "messenger", 'L' + MESSENGER + ';');
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    @SneakyThrows
    private Class<?> defineType(final ClassWriter cw) {
        return MethodHandles.privateLookupIn(definition.getType(), MethodHandles.lookup())
                .defineClass(cw.toByteArray());
    }
}
