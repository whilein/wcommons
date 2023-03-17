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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author whilein
 */
public class MethodMaker extends MethodVisitor {
    public MethodMaker(final MethodVisitor methodVisitor) {
        super(Opcodes.ASM9, methodVisitor);
    }

    public void visitInt(final int value) {
        if (value >= -1 && value <= 5) {
            super.visitInsn(ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            super.visitIntInsn(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            super.visitIntInsn(SIPUSH, value);
        } else {
            super.visitLdcInsn(value);
        }
    }

    public void visitLong(final long value) {
        if (value == 0) {
            super.visitInsn(LCONST_0);
        } else if (value == 1) {
            super.visitInsn(LCONST_1);
        } else {
            super.visitLdcInsn(value);
        }
    }

    public void visitFloat(final float value) {
        if (value == 0) {
            super.visitInsn(FCONST_0);
        } else if (value == 1) {
            super.visitInsn(FCONST_1);
        } else if (value == 2) {
            super.visitInsn(FCONST_2);
        } else {
            super.visitLdcInsn(value);
        }
    }

    public void visitDouble(final double value) {
        if (value == 0) {
            super.visitInsn(DCONST_0);
        } else if (value == 1) {
            super.visitInsn(DCONST_1);
        } else {
            super.visitLdcInsn(value);
        }
    }
}
