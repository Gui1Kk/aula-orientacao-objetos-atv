import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Testes unitários do UsuarioService.
 */
class UsuarioServiceTest {

    private val usuarioRepository = mockk<UsuarioRepository>()
    private val auditoriaService = mockk<AuditoriaService>(relaxed = true)

    private val usuarioService = UsuarioService(usuarioRepository, auditoriaService)

    private val testUsuario = Usuario(
        id = "user-1",
        nome = "Fulano",
        email = "fulano@email.com",
        senhaHash = "hash",
        papeis = setOf(Papel.ADMIN_INSTITUICAO),
        instituicaoId = "inst-1",
        ativo = true
    )

    @Test
    fun `buscarPorId retorna usuario existente`() {
        every { usuarioRepository.findById("user-1") } returns testUsuario

        val result = usuarioService.buscarPorId("user-1")

        assertEquals("Fulano", result.nome)
    }

    @Test
    fun `buscarPorId inexistente lanca 404`() {
        every { usuarioRepository.findById("nope") } returns null

        val exception = assertThrows<ApiException> {
            usuarioService.buscarPorId("nope")
        }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `criarParaInstituicao com email existente lanca 400`() {
        every { usuarioRepository.findByEmail("fulano@email.com") } returns testUsuario

        val exception = assertThrows<ApiException> {
            usuarioService.criarParaInstituicao(
                CreateUsuarioInstituicaoRequest("Novo", "fulano@email.com", listOf("OPERADOR")),
                "inst-1", "admin-1"
            )
        }
        assertEquals(400, exception.statusCode)
    }

    @Test
    fun `criarParaInstituicao com email novo cria usuario password-less`() {
        every { usuarioRepository.findByEmail(any()) } returns null
        every { usuarioRepository.insert(any()) } answers { firstArg() }

        val result = usuarioService.criarParaInstituicao(
            CreateUsuarioInstituicaoRequest("Novo Op", "novo@email.com", listOf("OPERADOR")),
            "inst-1", "admin-1"
        )

        assertEquals("novo@email.com", result.email)
        assertEquals(setOf(Papel.OPERADOR), result.papeis)
        assertEquals("inst-1", result.instituicaoId)
        assertEquals("", result.senhaHash) // password-less
    }

    @Test
    fun `criarParaInstituicao com papel invalido lanca 400`() {
        every { usuarioRepository.findByEmail(any()) } returns null

        val exception = assertThrows<ApiException> {
            usuarioService.criarParaInstituicao(
                CreateUsuarioInstituicaoRequest("Novo", "x@email.com", listOf("PAPEL_FAKE")),
                "inst-1", "admin-1"
            )
        }
        assertEquals(400, exception.statusCode)
    }

    @Test
    fun `atualizar usuario existente retorna true`() {
        every { usuarioRepository.findById("user-1") } returns testUsuario
        every { usuarioRepository.update("user-1", any()) } returns true

        val result = usuarioService.atualizar("user-1", UpdateUsuarioRequest(nome = "Novo Nome"), "admin-1")

        assertTrue(result)
        verify { auditoriaService.registrar(AcaoAuditoria.ATUALIZAR, "Usuario", "user-1", "admin-1") }
    }

    @Test
    fun `deletar usuario existente retorna true`() {
        every { usuarioRepository.findById("user-1") } returns testUsuario
        every { usuarioRepository.delete("user-1") } returns true

        val result = usuarioService.deletar("user-1", "admin-1")

        assertTrue(result)
        verify { auditoriaService.registrar(AcaoAuditoria.DELETAR, "Usuario", "user-1", "admin-1") }
    }

    @Test
    fun `listar retorna resposta paginada`() {
        every { usuarioRepository.findAll(1, 10, any()) } returns Pair(listOf(testUsuario), 1L)

        val result = usuarioService.listar(PaginationParams(1, 10), emptyMap())

        assertEquals(1, result.docs.size)
        assertEquals(1L, result.totalDocs)
        assertEquals(1, result.totalPages)
    }
}

