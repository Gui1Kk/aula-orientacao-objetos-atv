import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

/**
 * Testes unitários do SenhaService.
 */
class SenhaServiceTest {

    private val senhaRepository = mockk<SenhaRepository>()
    private val filaRepository = mockk<FilaRepository>()
    private val qrCodeRepository = mockk<QrCodeRepository>()
    private val auditoriaService = mockk<AuditoriaService>(relaxed = true)
    private val webSocketManager = mockk<WebSocketManager>(relaxed = true)

    private val senhaService = SenhaService(
        senhaRepository, filaRepository, qrCodeRepository, auditoriaService, webSocketManager
    )

    private val filaOnline = Fila(
        id = "fila-1",
        instituicaoId = "inst-1",
        nome = "Fila Online",
        tipoAtendimento = TipoAtendimento.ONLINE,
        ativa = true
    )

    private val filaPresencial = Fila(
        id = "fila-2",
        instituicaoId = "inst-1",
        nome = "Fila Presencial",
        tipoAtendimento = TipoAtendimento.PRESENCIAL,
        ativa = true
    )

    private val senhaAguardando = Senha(
        id = "senha-1",
        filaId = "fila-1",
        instituicaoId = "inst-1",
        usuarioId = "user-1",
        posicao = 1,
        status = StatusSenha.AGUARDANDO
    )

    private val senhaEmAtendimento = Senha(
        id = "senha-2",
        filaId = "fila-1",
        instituicaoId = "inst-1",
        usuarioId = "user-2",
        posicao = 2,
        status = StatusSenha.EM_ATENDIMENTO,
        operadorId = "operador-1"
    )

    @Test
    fun `criar senha em fila online sem duplicidade retorna senha`() {
        every { filaRepository.findById("fila-1") } returns filaOnline
        every { senhaRepository.findActiveByUsuarioAndFila("user-1", "fila-1") } returns null
        every { senhaRepository.countByFilaIdAndStatuses("fila-1", any()) } returns 0L
        every { senhaRepository.insert(any()) } answers { firstArg() }

        val result = senhaService.criar("fila-1", "user-1", CreateSenhaRequest())

        assertEquals(StatusSenha.AGUARDANDO, result.status)
        assertEquals("fila-1", result.filaId)
        assertEquals("user-1", result.usuarioId)
        assertEquals(1, result.posicao)
    }

    @Test
    fun `criar senha com duplicidade lanca 409`() {
        every { filaRepository.findById("fila-1") } returns filaOnline
        every { senhaRepository.findActiveByUsuarioAndFila("user-1", "fila-1") } returns senhaAguardando

        val exception = assertThrows<ApiException> {
            senhaService.criar("fila-1", "user-1", CreateSenhaRequest())
        }
        assertEquals(409, exception.statusCode)
    }

    @Test
    fun `criar senha em fila inativa lanca 400`() {
        val filaInativa = filaOnline.copy(ativa = false)
        every { filaRepository.findById("fila-1") } returns filaInativa

        val exception = assertThrows<ApiException> {
            senhaService.criar("fila-1", "user-1", CreateSenhaRequest())
        }
        assertEquals(400, exception.statusCode)
    }

    @Test
    fun `criar senha em fila inexistente lanca 404`() {
        every { filaRepository.findById("inexistente") } returns null

        val exception = assertThrows<ApiException> {
            senhaService.criar("inexistente", "user-1", CreateSenhaRequest())
        }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `criar senha presencial em fila sem QR Code funciona`() {
        every { filaRepository.findById("fila-2") } returns filaPresencial
        every { senhaRepository.countByFilaIdAndStatuses("fila-2", any()) } returns 3L
        every { senhaRepository.insert(any()) } answers { firstArg() }

        val result = senhaService.criarPresencial(
            "fila-2", CreateSenhaPresencialRequest("João"), "operador-1"
        )

        assertTrue(result.presencial)
        assertEquals("João", result.nomeCidadao)
        assertEquals(4, result.posicao)
    }

    @Test
    fun `chamar senha aguardando transiciona para EM_ATENDIMENTO`() {
        every { senhaRepository.findById("senha-1") } returns senhaAguardando andThen
            senhaAguardando.copy(status = StatusSenha.EM_ATENDIMENTO, mesa = "1", operadorId = "op-1")
        every { senhaRepository.update(any(), any()) } returns true
        every { filaRepository.findById("fila-1") } returns filaOnline

        val result = senhaService.chamar("senha-1", "1", "Mesa 1", "op-1")

        assertEquals(StatusSenha.EM_ATENDIMENTO, result.status)
        verify { senhaRepository.update("senha-1", match { it["status"] == StatusSenha.EM_ATENDIMENTO }) }
    }

    @Test
    fun `chamar senha em atendimento lanca 409`() {
        every { senhaRepository.findById("senha-2") } returns senhaEmAtendimento

        val exception = assertThrows<ApiException> {
            senhaService.chamar("senha-2", "1", null, "op-1")
        }
        assertEquals(409, exception.statusCode)
    }

    @Test
    fun `cancelar senha aguardando transiciona para CANCELADA`() {
        every { senhaRepository.findById("senha-1") } returns senhaAguardando andThen
            senhaAguardando.copy(status = StatusSenha.CANCELADA)
        every { senhaRepository.update(any(), any()) } returns true
        every { filaRepository.findById("fila-1") } returns filaOnline

        val result = senhaService.cancelar("senha-1", "user-1")

        assertEquals(StatusSenha.CANCELADA, result.status)
    }

    @Test
    fun `cancelar senha em atendimento lanca 409`() {
        every { senhaRepository.findById("senha-2") } returns senhaEmAtendimento

        val exception = assertThrows<ApiException> {
            senhaService.cancelar("senha-2", "user-2")
        }
        assertEquals(409, exception.statusCode)
    }

    @Test
    fun `finalizar senha em atendimento transiciona para FINALIZADA`() {
        every { senhaRepository.findById("senha-2") } returns senhaEmAtendimento andThen
            senhaEmAtendimento.copy(status = StatusSenha.FINALIZADA)
        every { senhaRepository.update(any(), any()) } returns true
        every { filaRepository.findById("fila-1") } returns filaOnline

        val result = senhaService.finalizar("senha-2", "operador-1")

        assertEquals(StatusSenha.FINALIZADA, result.status)
    }

    @Test
    fun `finalizar senha aguardando lanca 409`() {
        every { senhaRepository.findById("senha-1") } returns senhaAguardando

        val exception = assertThrows<ApiException> {
            senhaService.finalizar("senha-1", "operador-1")
        }
        assertEquals(409, exception.statusCode)
    }

    @Test
    fun `criar senha presencial em fila online sem QR sem validacao de QR`() {
        every { filaRepository.findById("fila-1") } returns filaOnline
        every { senhaRepository.findActiveByUsuarioAndFila("user-1", "fila-1") } returns null
        every { senhaRepository.countByFilaIdAndStatuses("fila-1", any()) } returns 5L
        every { senhaRepository.insert(any()) } answers { firstArg() }

        val result = senhaService.criar("fila-1", "user-1", CreateSenhaRequest())

        assertEquals(6, result.posicao)
    }
}

