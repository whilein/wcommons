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

package w.util.bytes;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ByteUnit {

    UNO(1L),
    KIBI(1024L),
    MEBI(1024L * 1024L),
    GIBI(1024L * 1024L * 1024L),
    TEBI(1024L * 1024L * 1024L * 1024L);
    //PEBI, EXBI, ZEBI, YOBI;

    long scale;

    ByteUnit(final long scale) {
        this.scale = scale;
    }

    public double toUno(final double bytes) {
        return bytes * scale;
    }

    public double toKibi(final double bytes) {
        return convert(bytes, KIBI);
    }

    public double toMebi(final double bytes) {
        return convert(bytes, MEBI);
    }

    public double toGibi(final double bytes) {
        return convert(bytes, GIBI);
    }

    public double toTebi(final double bytes) {
        return convert(bytes, TEBI);
    }

    public long toUno(final long bytes) {
        return bytes * scale;
    }

    public long toKibi(final long bytes) {
        return convert(bytes, KIBI);
    }

    public long toMebi(final long bytes) {
        return convert(bytes, MEBI);
    }

    public long toGibi(final long bytes) {
        return convert(bytes, GIBI);
    }

    public long toTebi(final long bytes) {
        return convert(bytes, TEBI);
    }

    public double convert(final double bytes, final ByteUnit to) {
        return to == this ? bytes : (bytes * scale) / to.scale;
    }

    public long convert(final long bytes, final ByteUnit to) {
        return to == this ? bytes : (bytes * scale) / to.scale;
    }

}
