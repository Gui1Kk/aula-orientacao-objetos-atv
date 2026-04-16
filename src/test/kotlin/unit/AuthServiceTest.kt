import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

/**
 * Testes unitários do AuthService.
 */
class AuthServiceTest {

    private val usuarioRepository = mockk<UsuarioRepository>()
    private val jwtConfig = mockk<JwtConfig>()
    private val auditoriaService = mockk<AuditoriaService>(relaxed = true)

    private val authService = AuthService(usuarioRepository, jwtConfig, auditoriaService)

    private val testUsuario = Usuario(
        id = "user-1",
        nome = "Teste",
        email = "teste@email.com",
        senhaHash = PasswordUtils.hashPassword("senha123"),
        papeis = setOf(Papel.USUARIO_FINAL),
        ativo = true
    )

    @Test
    fun `login com credenciais validas retorna tokens`() {
        every { usuarioRepository.findByEmail("teste@email.com") } returns testUsuario
        every { jwtConfig.generateAccessToken(any(), any(), any()) } returns "access-token"
        every { jwtConfig.generateRefreshToken(any()) } returns "refresh-token"
        every { jwtConfig.accessTokenExpiresAt() } returns Instant.now().toString()
        every { usuarioRepository.update(any(), any()) } returns true

        val result = authService.login(LoginRequest("teste@email.com", "senha123"))

        assertEquals("access-token", result.token)
        assertEquals("refresh-token", result.refresh)
        verify { usuarioRepository.findByEmail("teste@email.com") }
        verify { usuarioRepository.update("user-1", any()) }
    }

    @Test
    fun `login com email inexistente lanca ApiException 401`() {
        every { usuarioRepository.findByEmail("naoexiste@email.com") } returns null

        val exception = assertThrows<ApiException> {
            authService.login(LoginRequest("naoexiste@email.com", "senha"))
        }
        assertEquals(401, exception.statusCode)
    }

    @Test
    fun `login com senha incorreta lanca ApiException 401`() {
        every { usuarioRepository.findByEmail("teste@email.com") } returns testUsuario

        val exception = assertThrows<ApiException> {
            authService.login(LoginRequest("teste@email.com", "senhaErrada"))
        }
        assertEquals(401, exception.statusCode)
    }

    @Test
    fun `login com usuario desativado lanca ApiException 403`() {
        val usuarioInativo = testUsuario.copy(ativo = false)
        every { usuarioRepository.findByEmail("teste@email.com") } returns usuarioInativo

        val exception = assertThrows<ApiException> {
            authService.login(LoginRequest("teste@email.com", "senha123"))
        }
        assertEquals(403, exception.statusCode)
    }

    @Test
    fun `register com email existente lanca ApiException 409`() {
        every { usuarioRepository.findByEmail("teste@email.com") } returns testUsuario

        val exception = assertThrows<ApiException> {
            authService.register(RegisterRequest("Novo", "teste@email.com", "senha123"))
        }
        assertEquals(409, exception.statusCode)
    }

    @Test
    fun `register com email novo cria usuario e retorna tokens`() {
        every { usuarioRepository.findByEmail(any()) } returns null
        every { usuarioRepository.insert(any()) } answers { firstArg() }
        every { jwtConfig.generateAccessToken(any(), any(), any()) } returns "access-token"
        every { jwtConfig.generateRefreshToken(any()) } returns "refresh-token"
        every { jwtConfig.accessTokenExpiresAt() } returns Instant.now().toString()
        every { usuarioRepository.update(any(), any()) } returns true

        val result = authService.register(RegisterRequest("Novo User", "novo@email.com", "senha123"))

        assertEquals("access-token", result.token)
        verify { usuarioRepository.insert(match { it.email == "novo@email.com" }) }
    }

    @Test
    fun `logout limpa tokens do usuario`() {
        every { usuarioRepository.update("user-1", any()) } returns true

        authService.logout("user-1")

        verify { usuarioRepository.update("user-1", match {
            it["accesstoken"] == null && it["refreshtoken"] == null
        }) }
    }

    @Test
    fun `recover com email inexistente lanca ApiException 400`() {
        every { usuarioRepository.findByEmail("inexistente@email.com") } returns null

        val exception = assertThrows<ApiException> {
            authService.recover(RecoverRequest("inexistente@email.com"))
        }
        assertEquals(400, exception.statusCode)
    }

    @Test
    fun `recover com email valido gera token e codigo`() {
        every { usuarioRepository.findByEmail("teste@email.com") } returns testUsuario
        every { usuarioRepository.update(any(), any()) } returns true

        val result = authService.recover(RecoverRequest("teste@email.com"))

        assertNotNull(result["token"])
        assertNotNull(result["codigo"])
        assertEquals(Constants.CODIGO_RECUPERACAO_LENGTH, result["codigo"]!!.length)
    }
}

