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

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import w.util.ClassLoaderUtils;
import w.util.TypeUtils;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.objectweb.asm.Opcodes.IRETURN;

/**
 * @author whilein
 */
final class MakerTests {

    ClassMaker cm;

    @BeforeEach
    void tearUp() {
        cm = new ClassMaker(0);

        cm.visit(
                Opcodes.V1_1, Opcodes.ACC_PUBLIC,
                "Test",
                null,
                "java/lang/Object", null
        );
    }

    private Class<?> make() {
        val bytes = cm.toByteArray();

        return ClassLoaderUtils.defineClass(this.getClass().getClassLoader(), cm.name, bytes);
    }

    @Test
    @SneakyThrows
    void testEmptyConstructor() {
        cm.visitEmptyConstructor(Opcodes.ACC_PUBLIC);

        Object object = make().getConstructor().newInstance();
        assertNotNull(object);
    }

    private String getNumber(final Number number) {
        return ("getNumber" + number).replace('-', 'M').replace('.', '_');
    }

    @SneakyThrows
    <T extends Number> void testNumbers(final T[] numbers, final BiConsumer<MethodMaker, T> maker) {
        for (val number : numbers) {
            val primitive = TypeUtils.getPrimitive(number.getClass()).orElseThrow();
            val primitiveType = Type.getType(primitive);

            val mm = cm.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    getNumber(number), "()" + primitiveType.getDescriptor(), null, null);
            maker.accept(mm, number);
            mm.visitInsn(primitiveType.getOpcode(IRETURN));

            mm.visitMaxs(2, 0);
        }

        val cls = make();

        for (val test : numbers) {
            assertEquals(test, cls.getDeclaredMethod(getNumber(test)).invoke(null));
        }
    }

    @Test
    @SneakyThrows
    void testVisitFloat() {
        testNumbers(new Float[]{0f, 1f, 1.1f, 100f}, MethodMaker::visitFloat);
    }

    @Test
    @SneakyThrows
    void testVisitDouble() {
        testNumbers(new Double[]{0d, 1d, 1.1d, 100d}, MethodMaker::visitDouble);
    }

    @Test
    @SneakyThrows
    void testVisitLong() {
        testNumbers(new Long[]{0L, 1L, 100L}, MethodMaker::visitLong);
    }

    @Test
    @SneakyThrows
    void testVisitInt() {
        testNumbers(new Integer[]{-1, 0, 1, 2, 3, 4, 5, 100, 1000, 10000}, MethodMaker::visitInt);
    }

}
