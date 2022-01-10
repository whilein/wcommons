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

package w.util.ci;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.Root;

import java.lang.invoke.VarHandle;

/**
 * @author whilein
 */
@UtilityClass
public class CiStrings {

    private static final CiString EMPTY = new EmptyCiString();

    public @NotNull CiString empty() {
        return EMPTY;
    }

    public @NotNull CiString from(final @NotNull String another) {
        return another.length() > 0 ? TrustedCiString.create(another) : EMPTY;
    }


    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TrustedCiString implements CiString {

        private static final VarHandle STRING__CODER;
        private static final VarHandle STRING__VALUE;

        private static final int HI_BYTE_SHIFT;
        private static final int LO_BYTE_SHIFT;

        static {
            try {
                val lookup = Root.trustedLookupIn(String.class);
                STRING__CODER = lookup.findVarHandle(String.class, "coder", byte.class);
                STRING__VALUE = lookup.findVarHandle(String.class, "value", byte[].class);

                val stringUtf16 = lookup.findClass("java.lang.StringUTF16");

                LO_BYTE_SHIFT = (int) lookup.findStaticVarHandle(stringUtf16, "LO_BYTE_SHIFT", int.class).get();
                HI_BYTE_SHIFT = (int) lookup.findStaticVarHandle(stringUtf16, "HI_BYTE_SHIFT", int.class).get();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static final int LATIN_CASE_DIFF = 'A' - 'z';

        String original;

        byte[] value;

        byte coder;

        @NonFinal
        int hash;

        public int length() {
            return value.length >> coder;
        }

        public static CiString create(final String text) {
            return new TrustedCiString(text, (byte[]) STRING__VALUE.get(text), (byte) STRING__CODER.get(text));
        }

        private static char getChar(final byte[] val, int index) {
            index <<= 1;

            return (char)
                    (((val[index++] & 0xff) << HI_BYTE_SHIFT) |
                            ((val[index] & 0xff) << LO_BYTE_SHIFT));
        }

        private static boolean checkEqualityLatin(final byte[] which, final byte[] that) {
            for (int i = 0, j = which.length; i < j; i++) {
                val x = toLowerCase(which[i]);
                val y = toLowerCase(that[i]);

                if (x != y) {
                    return false;
                }
            }

            return true;
        }

        private static byte toLowerCase(byte v) {
            if (v >= 'a' && v <= 'z') {
                v -= LATIN_CASE_DIFF;
            }

            return v;
        }

        private static boolean checkEqualityUtf16(final byte[] which, final byte[] that) {
            for (int i = 0, j = which.length >> 1; i < j; i++) {
                val x = Character.toUpperCase(getChar(which, i));
                val y = Character.toUpperCase(getChar(that, i));

                if (x != y) {
                    return false;
                }
            }

            return true;
        }

        private static int computeUtf16Hash(final byte[] value) {
            int hash = 0;

            for (int i = 0, j = value.length >> 1; i < j; i++) {
                hash = 31 * hash + Character.toLowerCase(getChar(value, i));
            }

            return hash;
        }

        private static int computeLatinHash(final byte[] value) {
            int hash = 0;

            for (val v : value) {
                hash = 31 * hash + (toLowerCase(v) & 0xFF);
            }

            return hash;
        }

        @Override
        public @NotNull String toString() {
            return original;
        }

        @Override
        public int hashCode() {
            int h = hash;

            if (h == 0 && value.length > 0) {
                h = hash = coder == 0 ? computeLatinHash(value) : computeUtf16Hash(value);
            }

            return h;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof CiString)) return false;
            if (obj == this) return true;

            return _equals((CiString) obj);
        }

        @Override
        public boolean equals(final byte coder, final byte[] value) {
            return this.coder == coder && this.value.length == value.length && (coder == 0
                    ? checkEqualityLatin(value, this.value)
                    : checkEqualityUtf16(value, this.value));
        }

        private boolean _equals(final CiString that) {
            return that.equals(coder, value);
        }

        @Override
        public boolean equals(final CiString another) {
            return another != null && _equals(another);
        }

        @Override
        public boolean equals(final String another) {
            if (another == null) {
                return false;
            }

            final byte coder = (byte) STRING__CODER.get(another);
            final byte[] value = (byte[]) STRING__VALUE.get(another);

            return equals(coder, value);
        }

    }

    private static final class EmptyCiString implements CiString {

        @Override
        public int length() {
            return 0;
        }

        @Override
        public boolean equals(final byte coder, final byte[] value) {
            return coder == 0 && value.length == 0;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public @NotNull String toString() {
            return "";
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) return true;

            if (obj instanceof String) {
                return ((String) obj).length() == 0;
            } else if (obj instanceof CiString) {
                return ((CiString) obj).length() == 0;
            } else {
                return false;
            }
        }

        @Override
        public boolean equals(final CiString another) {
            return another != null && another.length() == 0;
        }

        @Override
        public boolean equals(final String another) {
            return another != null && another.length() == 0;
        }
    }

}
