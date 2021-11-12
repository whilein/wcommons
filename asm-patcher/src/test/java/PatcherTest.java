import org.junit.jupiter.api.Test;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import w.asm.patcher.AsmPatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static w.asm.patcher.Matchers.name;

/**
 * @author whilein
 */
final class AsmPatcherTests {

    @Test
    void testRedefineMethod() {
        assertEquals("Hello world", getText());

        AsmPatcher.redefine(AsmPatcherTests.class)
                .on(name("getText"))
                .intercept(mv -> new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override
                    public void visitLdcInsn(final Object value) {
                        super.visitLdcInsn("Соси хуй");
                    }
                })
                .apply();

        assertEquals("Соси хуй", getText());
    }

    String getText() {
        return "Hello world";
    }

}
