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

package w.annotation.index;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
@Dummy
final class IndexTests {

    @Dummy
    String test;

    @Dummy
    void test() {
    }

    @Test
    void testTypes() {
        val types = AnnotationIndex.getAnnotatedTypes(Dummy.class);
        assertEquals(1, types.size());
        assertEquals(IndexTests.class, types.get(0));
    }

    @Test
    void testFields() {
        val fields = AnnotationIndex.getAnnotatedFields(Dummy.class);
        assertEquals(1, fields.size());
        assertEquals("test", fields.get(0).getName());
        assertEquals(String.class, fields.get(0).getType());
    }

    @Test
    void testMethods() {
        val methods = AnnotationIndex.getAnnotatedMethods(Dummy.class);
        assertEquals(1, methods.size());
        assertEquals("test", methods.get(0).getName());
        assertEquals(void.class, methods.get(0).getReturnType());
    }

}
