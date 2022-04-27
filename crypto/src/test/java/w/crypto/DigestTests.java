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

package w.crypto;

import org.junit.jupiter.api.Test;
import w.crypto.digest.Digest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
final class DigestTests {

    private static final String VALUE
            = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer id sem consectetur, ullamcorper tortor malesuada.";

    @Test
    void testSha5_256() {
        assertEquals(
                "494ac71b6013cd9eabf79effd4eb019c39092a73c7358a16c339356044ff5573",
                Digest.SHA5_256.digest(VALUE)
        );
    }

    @Test
    void testSha5_224() {
        assertEquals(
                "251ac095d4fa4e51ce574789bbe6439f7b44694119577c9abb9f2a8a",
                Digest.SHA5_224.digest(VALUE)
        );
    }

    @Test
    void testSha5_512() {
        assertEquals(
                "d09820e924df5debafafd0df1185467a4eca5f4259df5bba2475c2625329ee675ad7a18259495f09a8f731c79fb62680ab744e5557dc80dc49e4515991b7e20a",
                Digest.SHA5_512.digest(VALUE)
        );
    }

    @Test
    void testMd5() {
        assertEquals(
                "413d9325f4e75eb6e01bf696d14b4f28",
                Digest.MD5.digest(VALUE)
        );
    }

    @Test
    void testSha_256() {
        assertEquals(
                "1f86945a8364275a3ae5f03c094b97346217992d4e98aa21abfc183921db95a0",
                Digest.SHA_256.digest(VALUE)
        );
    }

    @Test
    void testSha3_224() {
        assertEquals(
                "f0ec3a55b7939457eab420d12cd9be3fa5a5095d9e9f2cfe1969359f",
                Digest.SHA3_224.digest(VALUE)
        );
    }

    @Test
    void testSha3_256() {
        assertEquals(
                "8cb214ce9f9850b9e2e960adace84fa29e17a335c477e559ed09ddd7de1a7f75",
                Digest.SHA3_256.digest(VALUE)
        );
    }

    @Test
    void testSha3_384() {
        assertEquals(
                "9a88642d185939392154bec1cc738a2442bd6106a8811c51972b02c1f173407e42e3f5081490035bd636beb9ee05224e",
                Digest.SHA3_384.digest(VALUE)
        );
    }

    @Test
    void testSha3_512() {
        assertEquals(
                "1cbd6ed223de096ab1756acdd10ed967a9bf23cbe35397af243e450c0faca536b671451404630123a9eb2eb2e34a6dd177628b3fb8cfa89a174c15eeebd1ca4c",
                Digest.SHA3_512.digest(VALUE)
        );
    }
}
