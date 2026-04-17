import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
enum class StatusSenha {
    AGUARDANDO,
    EM_ATENDIMENTO,
    CANCELADA,
    FINALIZADA
}

@Serializable
enum class Prioridade(val peso: Int) {
    LEGAL(1),
    FIDELIDADE(2),
    CRONOLOGICA(3);

    fun descricao(): String = when (this) {
        LEGAL -> "Prioridade legal (idoso, PCD, gestante)"
        FIDELIDADE -> "Prioridade por fidelidade"
        CRONOLOGICA -> "Ordem de chegada"
    }
}

@Serializable
data class Senha(
    val id: String? = null,
    val filaId: String,
    val instituicaoId: String,
    val usuarioId: String? = null,
    val nomeCidadao: String? = null,
    val presencial: Boolean = false,
    val posicao: Int,
    val status: StatusSenha = StatusSenha.AGUARDANDO,
    val prioridade: String? = null,
    val mesa: String? = null,
    val mesaNome: String? = null,
    val operadorId: String? = null,
    @Contextual val createdAt: Instant = Instant.now(),
    @Contextual val updatedAt: Instant? = null
)
