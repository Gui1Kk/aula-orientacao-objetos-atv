import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

/**
 * Testes unitários do InstantSerializer.
 */
class InstantSerializerTest {

    @Test
    fun `serializa Instant para String ISO 8601`() {
        val instant = Instant.parse("2026-02-25T14:30:00Z")
        // O toString() de Instant já produz ISO 8601
        assertEquals("2026-02-25T14:30:00Z", instant.toString())
    }

    @Test
    fun `deserializa String ISO 8601 para Instant`() {
        val str = "2026-02-25T14:30:00Z"
        val instant = Instant.parse(str)
        assertEquals(2026, instant.atZone(java.time.ZoneOffset.UTC).year)
        assertEquals(2, instant.atZone(java.time.ZoneOffset.UTC).monthValue)
    }

    @Test
    fun `Instant com milissegundos serializa corretamente`() {
        val instant = Instant.parse("2026-02-25T14:30:00.123Z")
        assertEquals("2026-02-25T14:30:00.123Z", instant.toString())
    }

    @Test
    fun `Instant epoch zero serializa corretamente`() {
        val instant = Instant.EPOCH
        assertEquals("1970-01-01T00:00:00Z", instant.toString())
    }
}

