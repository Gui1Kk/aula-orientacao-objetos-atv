import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.time.Instant

@Serializable
enum class StatusInstituicao {
    PENDENTE,
    APROVADA,
    REJEITADA
}

@Serializable
data class Instituicao(
    val id: String? = null,
    val nome: String,
    val cnpj: String = "",
    val email: String = "",
    val telefone: String = "",
    val responsavel: String = "",
    val endereco: String = "",
    val descricao: String = "",
    val ativo: Boolean = true,
    val status: StatusInstituicao = StatusInstituicao.PENDENTE,
    val solicitanteId: String? = null,
    val contratoUrl: String = "",
    val motivoRejeicao: String = "",
    val aprovadoPor: String? = null,
    @Contextual val aprovadoEm: Instant? = null,
    val configuracoes: Map<String, JsonElement> = emptyMap(),
    @Contextual val createdAt: Instant = Instant.now(),
    @Contextual val updatedAt: Instant? = null
)
