import org.junit.jupiter.api.Test;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import w.asm.patcher.AsmPatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static w.asm.patcher.Matchers.named;

/**
 * @author whilein
 */
final class AsmPatcherTests {

    static final String BEFORE_REDEFINE = "Соси хуй";
    static final String AFTER_REDEFINE = "Гей тупой";

    @Test
    void testRedefineMethod() {
        assertEquals(BEFORE_REDEFINE, getText());

        AsmPatcher.redefine(AsmPatcherTests.class)
                .on(named("getText"))
                .intercept(mv -> new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override
                    public void visitLdcInsn(final Object value) {
                        super.visitLdcInsn(value.equals(BEFORE_REDEFINE) ? AFTER_REDEFINE : value);
                    }
                })
                .apply();

        assertEquals(AFTER_REDEFINE, getText());
    }

    String getText() {
        return BEFORE_REDEFINE;
    }

}
