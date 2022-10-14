package w.config

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author whilein
 */
class JacksonConfigProviderTests {

    @Test
    fun testAsKotlinClass() {
        val json = "{\"value\": \"Test value\"}"

        val kotlinClass = JsonConfigProvider.INSTANCE.load(json, KotlinClass::class.java)
        assertEquals("Test value", kotlinClass.value)
    }

    class KotlinClass(val value: String)

    companion object {
        @JvmStatic
        @BeforeAll
        fun testKotlinSupport() {
            assertTrue(KotlinSupport.isAvailable())
        }
    }

}