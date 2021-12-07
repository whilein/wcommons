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

package w.util.pair;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author whilein
 */
final class PairTests {

    @Test
    void testClone() {
        val pair = Pair.of("foo", "bar");
        val clonedPair = pair.clone();

        assertNotEquals(System.identityHashCode(pair), System.identityHashCode(clonedPair));
    }

    @Test
    void testDeepClone() {
        val left = new ArrayList<>(List.of("foo", "bar", "baz"));
        val right = new ArrayList<>(List.of("foo", "bar", "baz"));

        val pair = Pair.of(left, right);
        val clonedPair = pair.deepClone();

        assertNotEquals(System.identityHashCode(pair), System.identityHashCode(clonedPair));
        assertNotEquals(System.identityHashCode(pair.getLeft()), System.identityHashCode(clonedPair.getLeft()));
        assertNotEquals(System.identityHashCode(pair.getRight()), System.identityHashCode(clonedPair.getRight()));
    }

}
