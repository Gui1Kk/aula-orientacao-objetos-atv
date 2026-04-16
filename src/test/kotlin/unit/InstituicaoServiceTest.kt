import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

/**
 * Testes unitários do InstituicaoService.
 */
class InstituicaoServiceTest {

    private val instituicaoRepository = mockk<InstituicaoRepository>()
    private val auditoriaService = mockk<AuditoriaService>(relaxed = true)

    private val instituicaoService = InstituicaoService(instituicaoRepository, auditoriaService)

    private val instPendente = Instituicao(
        id = "inst-1",
        nome = "Hospital Teste",
        status = StatusInstituicao.PENDENTE,
        solicitanteId = "user-1"
    )

    private val instAprovada = Instituicao(
        id = "inst-2",
        nome = "Escola Teste",
        status = StatusInstituicao.APROVADA,
        ativo = true
    )

    private val instRejeitada = Instituicao(
        id = "inst-3",
        nome = "Clínica Teste",
        status = StatusInstituicao.REJEITADA,
        motivoRejeicao = "Documentação incompleta"
    )

    @Test
    fun `criar instituicao com nome unico retorna instituicao`() {
        every { instituicaoRepository.findByNome("Nova Inst") } returns null
        every { instituicaoRepository.insert(any()) } answers { firstArg() }

        val result = instituicaoService.criar(CreateInstituicaoRequest("Nova Inst"), "admin-1")

        assertEquals("Nova Inst", result.nome)
        assertEquals(StatusInstituicao.APROVADA, result.status)
    }

    @Test
    fun `criar instituicao com nome duplicado lanca 400`() {
        every { instituicaoRepository.findByNome("Hospital Teste") } returns instPendente

        val exception = assertThrows<ApiException> {
            instituicaoService.criar(CreateInstituicaoRequest("Hospital Teste"), "admin-1")
        }
        assertEquals(400, exception.statusCode)
    }

    @Test
    fun `aprovar instituicao pendente muda status para APROVADA`() {
        every { instituicaoRepository.findById("inst-1") } returns instPendente andThen
            instPendente.copy(status = StatusInstituicao.APROVADA, ativo = true)
        every { instituicaoRepository.update(any(), any()) } returns true

        val result = instituicaoService.aprovar("inst-1", "admin-1")

        assertEquals(StatusInstituicao.APROVADA, result.status)
        verify { instituicaoRepository.update("inst-1", match {
            it["status"] == StatusInstituicao.APROVADA && it["ativo"] == true
        }) }
    }

    @Test
    fun `aprovar instituicao nao pendente lanca 409`() {
        every { instituicaoRepository.findById("inst-2") } returns instAprovada

        val exception = assertThrows<ApiException> {
            instituicaoService.aprovar("inst-2", "admin-1")
        }
        assertEquals(409, exception.statusCode)
    }

    @Test
    fun `rejeitar instituicao pendente muda status para REJEITADA`() {
        every { instituicaoRepository.findById("inst-1") } returns instPendente andThen
            instPendente.copy(status = StatusInstituicao.REJEITADA, motivoRejeicao = "Motivo")
        every { instituicaoRepository.update(any(), any()) } returns true

        val result = instituicaoService.rejeitar("inst-1", "Motivo", "admin-1")

        assertEquals(StatusInstituicao.REJEITADA, result.status)
    }

    @Test
    fun `reconsiderar instituicao rejeitada volta para PENDENTE`() {
        every { instituicaoRepository.findById("inst-3") } returns instRejeitada andThen
            instRejeitada.copy(status = StatusInstituicao.PENDENTE, motivoRejeicao = "")
        every { instituicaoRepository.update(any(), any()) } returns true

        val result = instituicaoService.reconsiderar("inst-3", "admin-1")

        assertEquals(StatusInstituicao.PENDENTE, result.status)
    }

    @Test
    fun `reconsiderar instituicao nao rejeitada lanca 404`() {
        every { instituicaoRepository.findById("inst-1") } returns instPendente

        val exception = assertThrows<ApiException> {
            instituicaoService.reconsiderar("inst-1", "admin-1")
        }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `buscarPorId inexistente lanca 404`() {
        every { instituicaoRepository.findById("nope") } returns null

        val exception = assertThrows<ApiException> {
            instituicaoService.buscarPorId("nope")
        }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `solicitar cria instituicao com status PENDENTE`() {
        every { instituicaoRepository.insert(any()) } answers { firstArg() }

        val result = instituicaoService.solicitar(
            SolicitarInstituicaoRequest("Inst Solicitada", cnpj = "12345"),
            "user-1"
        )

        assertEquals(StatusInstituicao.PENDENTE, result.status)
        assertEquals("user-1", result.solicitanteId)
        assertEquals("12345", result.cnpj)
    }
}

