import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Testes unitários do FilaService.
 */
class FilaServiceTest {

    private val filaRepository = mockk<FilaRepository>()
    private val senhaRepository = mockk<SenhaRepository>()
    private val auditoriaService = mockk<AuditoriaService>(relaxed = true)
    private val webSocketManager = mockk<WebSocketManager>(relaxed = true)

    private val filaService = FilaService(filaRepository, senhaRepository, auditoriaService, webSocketManager)

    private val filaAtiva = Fila(
        id = "fila-1",
        instituicaoId = "inst-1",
        nome = "Fila Atendimento",
        tipoAtendimento = TipoAtendimento.ONLINE,
        ativa = true
    )

    @Test
    fun `buscarPorId retorna fila existente`() {
        every { filaRepository.findById("fila-1") } returns filaAtiva

        val result = filaService.buscarPorId("fila-1")

        assertEquals("Fila Atendimento", result.nome)
    }

    @Test
    fun `buscarPorId inexistente lanca 404`() {
        every { filaRepository.findById("nope") } returns null

        val exception = assertThrows<ApiException> {
            filaService.buscarPorId("nope")
        }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `criar fila online sem QR Code config`() {
        every { filaRepository.insert(any()) } answers { firstArg() }

        val result = filaService.criar(
            CreateFilaRequest(nome = "Nova Fila", tipoAtendimento = "ONLINE"),
            "inst-1", "admin-1"
        )

        assertEquals("Nova Fila", result.nome)
        assertEquals(TipoAtendimento.ONLINE, result.tipoAtendimento)
        assertNull(result.configuracaoQRCode)
    }

    @Test
    fun `criar fila com tipo atendimento invalido lanca 400`() {
        val exception = assertThrows<ApiException> {
            filaService.criar(
                CreateFilaRequest(nome = "Fila", tipoAtendimento = "INVALIDO"),
                "inst-1", "admin-1"
            )
        }
        assertEquals(400, exception.statusCode)
    }

    @Test
    fun `criar fila presencial com configuracao QR Code`() {
        every { filaRepository.insert(any()) } answers { firstArg() }

        val result = filaService.criar(
            CreateFilaRequest(
                nome = "Fila Presencial",
                tipoAtendimento = "PRESENCIAL",
                configuracaoQRCode = ConfiguracaoQRCodeRequest(
                    modoQRCode = "ROTATIVO",
                    tempoExpiracaoMin = 5,
                    toleranciaMin = 2
                ),
                mesas = listOf(MesaRequest("1", "Mesa 1"), MesaRequest("2", "Mesa 2"))
            ),
            "inst-1", "admin-1"
        )

        assertNotNull(result.configuracaoQRCode)
        assertEquals(ModoQRCode.ROTATIVO, result.configuracaoQRCode!!.modoQRCode)
        assertEquals(2, result.mesas.size)
    }

    @Test
    fun `contagemSenhas retorna contagem de aguardando`() {
        every { filaRepository.findById("fila-1") } returns filaAtiva
        every { senhaRepository.countByFilaIdAndStatus("fila-1", StatusSenha.AGUARDANDO) } returns 5L

        val result = filaService.contagemSenhas("fila-1")

        assertEquals(5L, result.aguardando)
    }

    @Test
    fun `deletar fila existente retorna true`() {
        every { filaRepository.findById("fila-1") } returns filaAtiva
        every { filaRepository.delete("fila-1") } returns true

        val result = filaService.deletar("fila-1", "admin-1")

        assertTrue(result)
    }

    @Test
    fun `listar retorna resposta paginada`() {
        every { filaRepository.findAll(1, 10, any()) } returns Pair(listOf(filaAtiva), 1L)

        val result = filaService.listar(PaginationParams(1, 10), emptyMap())

        assertEquals(1, result.docs.size)
    }
}

