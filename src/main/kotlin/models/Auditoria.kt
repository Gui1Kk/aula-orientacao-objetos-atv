import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.time.Instant

@Serializable
enum class AcaoAuditoria {
    LOGIN,
    LOGOUT,
    CRIAR,
    ATUALIZAR,
    DELETAR,
    APROVAR,
    REJEITAR,
    RECONSIDERAR,
    CANCELAR,
    FINALIZAR,
    CHAMAR,
    DESATIVAR,
    REGENERAR,
    ALTERAR_PERFIL
}

@Serializable
data class Auditoria(
    val id: String? = null,
    val instituicaoId: String? = null,
    val usuarioId: String? = null,
    val acao: AcaoAuditoria,
    val entidade: String,
    val entidadeId: String? = null,
    val dados: Map<String, JsonElement>? = null,
    @Contextual val createdAt: Instant = Instant.now()
)
