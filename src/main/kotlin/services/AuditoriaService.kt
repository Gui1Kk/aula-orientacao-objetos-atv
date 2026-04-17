import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant
import java.util.UUID

class AuditoriaService(
    private val auditoriaRepository: AuditoriaRepository
) {
    fun registrar(
        acao: AcaoAuditoria,
        entidade: String,
        entidadeId: String? = null,
        usuarioId: String? = null,
        instituicaoId: String? = null,
        dados: Map<String, String>? = null
    ): Auditoria {
        val dadosJson: Map<String, JsonElement>? = dados?.mapValues { (_, value) -> JsonPrimitive(value) }
        val auditoria = Auditoria(
            id = UUID.randomUUID().toString(),
            instituicaoId = instituicaoId,
            usuarioId = usuarioId,
            acao = acao,
            entidade = entidade,
            entidadeId = entidadeId,
            dados = dadosJson,
            createdAt = Instant.now()
        )
        return auditoriaRepository.insert(auditoria)
    }

    fun listar(
        pagination: PaginationParams,
        filters: Map<String, Any?> = emptyMap()
    ): PaginatedResponse<Auditoria> {
        val (docs, total) = auditoriaRepository.findAll(pagination.page, pagination.limit, filters)
        return buildPaginatedResponse(docs, total, pagination)
    }
}
