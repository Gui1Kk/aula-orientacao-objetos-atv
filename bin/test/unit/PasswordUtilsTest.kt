import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Testes unitários do PasswordUtils.
 */
class PasswordUtilsTest {

    @Test
    fun `hashPassword gera hash diferente do texto original`() {
        val hash = PasswordUtils.hashPassword("senha123")
        assertNotEquals("senha123", hash)
        assertTrue(hash.startsWith("\$2a\$"))
    }

    @Test
    fun `verifyPassword retorna true para senha correta`() {
        val hash = PasswordUtils.hashPassword("senha123")
        assertTrue(PasswordUtils.verifyPassword("senha123", hash))
    }

    @Test
    fun `verifyPassword retorna false para senha incorreta`() {
        val hash = PasswordUtils.hashPassword("senha123")
        assertFalse(PasswordUtils.verifyPassword("senhaErrada", hash))
    }

    @Test
    fun `dois hashes da mesma senha sao diferentes (salt unico)`() {
        val hash1 = PasswordUtils.hashPassword("mesmaSenha")
        val hash2 = PasswordUtils.hashPassword("mesmaSenha")
        assertNotEquals(hash1, hash2)
        // Ambos devem validar
        assertTrue(PasswordUtils.verifyPassword("mesmaSenha", hash1))
        assertTrue(PasswordUtils.verifyPassword("mesmaSenha", hash2))
    }

    @Test
    fun `hashPassword com senha vazia funciona`() {
        val hash = PasswordUtils.hashPassword("")
        assertTrue(PasswordUtils.verifyPassword("", hash))
        assertFalse(PasswordUtils.verifyPassword("qualquerCoisa", hash))
    }
}

